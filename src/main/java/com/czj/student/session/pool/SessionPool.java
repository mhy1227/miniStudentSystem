package com.czj.student.session.pool;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;
import java.util.Queue;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 会话池核心类
 */
@Component
public class SessionPool {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionPool.class);
    
    // 默认配置
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30分钟
    // TODO: 后续添加监控告警功能
    // private static final double WARN_THRESHOLD = 0.8; // 80%容量报警阈值
    
    // 空闲会话队列
    private final Queue<UserSession> idlePool;
    
    // 活跃会话Map
    private final Map<String, UserSession> activePool;
    
    // 学号到会话ID的映射
    private final Map<String, String> snoToSessionId;
    
    // 配置信息
    private final int maxTotal;
    private final int maxIdle;
    private final int minIdle;
    private final long maxWaitMillis;
    private final long sessionTimeout;
    
    // 并发控制
    private final Semaphore semaphore;
    private final Lock maintainLock = new ReentrantLock();
    private final Lock sessionLock = new ReentrantLock();  // 用于会话状态变更
    
    // 统计信息
    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AtomicInteger borrowedCount = new AtomicInteger(0);
    private final AtomicInteger returnedCount = new AtomicInteger(0);
    private final AtomicInteger discardedCount = new AtomicInteger(0);
    
    // 池状态
    private volatile boolean closed = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public SessionPool(int maxTotal, int maxIdle) {
        this(maxTotal, maxIdle, DEFAULT_MIN_IDLE, 5000L, DEFAULT_SESSION_TIMEOUT);
    }
    
    public SessionPool(int maxTotal, int maxIdle, int minIdle, long maxWaitMillis, long sessionTimeout) {
        if (maxTotal <= 0 || maxIdle <= 0 || minIdle < 0 || maxWaitMillis <= 0 || sessionTimeout <= 0) {
            throw new IllegalArgumentException("Invalid pool configuration");
        }
        if (maxIdle > maxTotal) {
            throw new IllegalArgumentException("maxIdle cannot be greater than maxTotal");
        }
        if (minIdle > maxIdle) {
            throw new IllegalArgumentException("minIdle cannot be greater than maxIdle");
        }
        
        this.maxTotal = maxTotal;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
        this.maxWaitMillis = maxWaitMillis;
        this.sessionTimeout = sessionTimeout;
        
        this.idlePool = new ConcurrentLinkedQueue<>();
        this.activePool = new ConcurrentHashMap<>();
        this.snoToSessionId = new ConcurrentHashMap<>();
        this.semaphore = new Semaphore(maxTotal);
        
        // 初始化最小空闲会话
        for (int i = 0; i < minIdle; i++) {
            idlePool.offer(createSession());
        }
    }
    
    /**
     * 获取会话
     */
    public UserSession borrowSession(String sno) throws SessionException {
        checkPoolState();
        if (sno == null || sno.trim().isEmpty()) {
            throw new IllegalArgumentException("sno cannot be null or empty");
        }
        
        sessionLock.lock();
        try {
            // 1. 检查是否已存在会话
            String existingSessionId = snoToSessionId.get(sno);
            if (existingSessionId != null) {
                UserSession existingSession = activePool.get(existingSessionId);
                if (existingSession != null) {
                    throw new SessionException("该账号已在其他地方登录");
                }
                // 如果会话不存在，清理映射
                snoToSessionId.remove(sno);
            }
            
            // 2. 尝试获取信号量
            if (!semaphore.tryAcquire(maxWaitMillis, TimeUnit.MILLISECONDS)) {
                throw new SessionException("获取会话超时");
            }
            
            try {
                // 3. 检查是否超过最大会话数
                if (activePool.size() >= maxTotal) {
                    throw new SessionException("会话池已满");
                }
                
                // 4. 尝试从空闲池获取
                UserSession session = idlePool.poll();
                
                // 5. 如果没有空闲会话，创建新的
                if (session == null) {
                    session = createSession();
                }
                
                // 6. 初始化会话
                initializeSession(session, sno);
                
                // 7. 添加到活跃池和映射
                activePool.put(session.getSessionId(), session);
                snoToSessionId.put(sno, session.getSessionId());
                
                // 8. 更新统计信息
                borrowedCount.incrementAndGet();
                
                return session;
            } catch (Exception e) {
                semaphore.release();
                throw new SessionException("获取会话失败", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SessionException("获取会话被中断", e);
        } finally {
            sessionLock.unlock();
        }
    }
    
    /**
     * 初始化会话
     */
    private void initializeSession(UserSession session, String sno) {
        session.reset(); // 确保会话状态干净
        session.setInPool(true);
        session.setSno(sno);
        session.setSessionId(generateSessionId());
        session.setCreateTime(System.currentTimeMillis());
        session.setLastAccessTime(System.currentTimeMillis());
        session.touch();
    }
    
    /**
     * 更新会话访问时间
     */
    public void updateSessionActivity(String sno) {
        if (sno == null) {
            return;
        }
        
        sessionLock.lock();
        try {
            String sessionId = snoToSessionId.get(sno);
            if (sessionId != null) {
                UserSession session = activePool.get(sessionId);
                if (session != null && isSessionValid(session)) {
                    session.touch();
                } else {
                    // 会话无效，清理相关资源
                    invalidateSession(sno);
                }
            }
        } finally {
            sessionLock.unlock();
        }
    }
    
    /**
     * 归还会话
     */
    public void returnSession(UserSession session) {
        if (session == null || !session.isInPool()) {
            return;
        }
        
        sessionLock.lock();
        try {
            // 1. 从活跃池和映射中移除
            activePool.remove(session.getSessionId());
            snoToSessionId.remove(session.getSno());
            
            // 2. 检查是否可以重用
            if (!isSessionValid(session)) {
                discardSession(session);
                return;
            }
            
            // 3. 检查空闲池大小
            if (idlePool.size() >= maxIdle) {
                discardSession(session);
                return;
            }
            
            // 4. 重置会话状态
            session.reset();
            
            // 5. 放入空闲池
            idlePool.offer(session);
            
            // 6. 更新统计信息
            returnedCount.incrementAndGet();
        } finally {
            if (session.isInPool()) {
                semaphore.release();
                session.setInPool(false);
            }
            sessionLock.unlock();
        }
    }
    
    /**
     * 丢弃会话
     */
    private void discardSession(UserSession session) {
        session.reset();
        session.setInPool(false);
        discardedCount.incrementAndGet();
    }
    
    /**
     * 定时维护任务
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void maintain() {
        if (closed || !maintainLock.tryLock()) {
            return;
        }
        
        try {
            checkPoolState();
            sessionLock.lock();
            try {
                // 1. 清理过期会话
                long now = System.currentTimeMillis();
                activePool.entrySet().removeIf(entry -> {
                    UserSession session = entry.getValue();
                    if (now - session.getLastAccessTime() > sessionTimeout) {
                        snoToSessionId.remove(session.getSno());
                        discardSession(session);
                        semaphore.release();
                        return true;
                    }
                    return false;
                });
                
                // 2. 确保最小空闲会话数
                while (idlePool.size() < minIdle) {
                    idlePool.offer(createSession());
                }
                
                // 3. 清理多余的空闲会话
                while (idlePool.size() > maxIdle) {
                    UserSession session = idlePool.poll();
                    if (session != null) {
                        discardSession(session);
                    }
                }
            } finally {
                sessionLock.unlock();
            }
        } finally {
            maintainLock.unlock();
        }
    }
    
    /**
     * 检查会话是否有效
     */
    public boolean isValidSession(String sno, String sessionId) {
        if (sno == null || sessionId == null) {
            return false;
        }
        
        String currentSessionId = snoToSessionId.get(sno);
        if (currentSessionId == null || !currentSessionId.equals(sessionId)) {
            return false;
        }
        
        UserSession session = activePool.get(sessionId);
        return isSessionValid(session);
    }
    
    /**
     * 获取当前登录IP
     */
    public String getCurrentLoginIp(String sno) {
        if (sno == null) {
            return null;
        }
        
        String sessionId = snoToSessionId.get(sno);
        if (sessionId != null) {
            UserSession session = activePool.get(sessionId);
            if (session != null) {
                return session.getIp();
            }
        }
        return null;
    }
    
    /**
     * 创建新会话
     */
    private UserSession createSession() {
        UserSession session = new UserSession();
        session.setSessionId(generateSessionId());
        session.setLastAccessTime(System.currentTimeMillis());
        createdCount.incrementAndGet();
        return session;
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 验证会话是否有效
     */
    private boolean isSessionValid(UserSession session) {
        return session != null && 
               System.currentTimeMillis() - session.getLastAccessTime() <= sessionTimeout;
    }
    
    /**
     * 获取统计信息
     */
    public SessionStats getStats() {
        return new SessionStats(
            createdCount.get(),
            borrowedCount.get(),
            returnedCount.get(),
            discardedCount.get(),
            activePool.size(),
            idlePool.size()
        );
    }
    
    /**
     * 使会话无效（用于异地登录踢出等场景）
     */
    public void invalidateSession(String sno) {
        if (sno == null) {
            return;
        }
        
        sessionLock.lock();
        try {
            String sessionId = snoToSessionId.get(sno);
            if (sessionId != null) {
                UserSession session = activePool.remove(sessionId);
                snoToSessionId.remove(sno);
                if (session != null) {
                    discardSession(session);
                    semaphore.release();
                }
            }
        } finally {
            sessionLock.unlock();
        }
    }
    
    /**
     * 检查池状态
     */
    private void checkPoolState() {
        if (closed) {
            throw new IllegalStateException("Session pool is closed");
        }
        
        // TODO: 后续添加容量监控和告警
        /* 
        double usage = (double) activePool.size() / maxTotal;
        if (usage >= WARN_THRESHOLD) {
            logger.warn("Session pool is nearly full: {}/{} ({:.1f}%)", 
                activePool.size(), maxTotal, usage * 100);
        }
        */
    }
    
    /**
     * 获取池状态信息
     */
    // TODO: 后续添加池状态监控功能
    /*
    public PoolStatus getPoolStatus() {
        return new PoolStatus(
            maxTotal,
            activePool.size(),
            idlePool.size(),
            (double) activePool.size() / maxTotal,
            closed
        );
    }
    */
    
    @Override
    public String toString() {
        return String.format(
            "SessionPool{active=%d/%d, idle=%d/%d, created=%d, borrowed=%d, returned=%d, discarded=%d}",
            activePool.size(), maxTotal,
            idlePool.size(), maxIdle,
            createdCount.get(), borrowedCount.get(),
            returnedCount.get(), discardedCount.get()
        );
    }
    
    /**
     * 关闭会话池
     */
    @PreDestroy
    public void shutdown() {
        closed = true;
        
        // 停止定时任务
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
        
        // 清理所有会话
        sessionLock.lock();
        try {
            // 清理活跃会话
            for (UserSession session : activePool.values()) {
                discardSession(session);
                semaphore.release();
            }
            activePool.clear();
            snoToSessionId.clear();
            
            // 清理空闲会话
            UserSession session;
            while ((session = idlePool.poll()) != null) {
                discardSession(session);
            }
        } finally {
            sessionLock.unlock();
        }
        
        logger.info("Session pool shutdown completed");
    }
} 