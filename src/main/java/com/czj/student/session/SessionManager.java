package com.czj.student.session;

import java.util.Date;
import com.czj.student.session.pool.SessionPool;
import com.czj.student.session.pool.UserSession;
import com.czj.student.session.pool.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 会话管理器，用于处理用户登录会话
 * 实现异地登录检测功能
 */
@Component
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    @Resource
    private SessionPool sessionPool;
    
    // 会话超时时间（30分钟）
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;
    
    private final Lock sessionLock = new ReentrantLock();
    private final ConcurrentHashMap<String, UserSession> activePool = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> snoToSessionId = new ConcurrentHashMap<>();
    private final AtomicInteger borrowedCount = new AtomicInteger();
    
    /**
     * 处理用户登录
     * @param sno 学号
     * @param sessionId 会话ID
     * @param ip 登录IP
     * @return 登录结果
     */
    public boolean login(String sno, String sessionId, String ip) {
        try {
            logger.info("尝试登录 - 学号: {}, 会话ID: {}, IP: {}", sno, sessionId, ip);
            // 使用传入的sessionId(JSESSIONID)获取会话
            UserSession session = sessionPool.borrowSession(sno, sessionId);
            
            // 设置会话信息
            session.setIp(ip);
            session.setLoginTime(new Date());
            
            logger.info("用户[{}]从IP[{}]登录成功", sno, ip);
            return true;
        } catch (SessionException e) {
            logger.info("用户[{}]在IP[{}]尝试登录，但已在IP[{}]登录", 
                sno, ip, sessionPool.getCurrentLoginIp(sno));
            return false;
        }
    }
    
    /**
     * 处理用户登出
     * @param sno 学号
     */
    public void logout(String sno) {
        try {
            String ip = sessionPool.getCurrentLoginIp(sno);
            sessionPool.invalidateSession(sno);
            if (ip != null) {
                logger.info("用户[{}]从IP[{}]登出", sno, ip);
            }
        } catch (Exception e) {
            logger.error("处理用户[{}]登出时发生异常", sno, e);
        }
    }
    
    /**
     * 强制用户下线
     * @param sno 学号
     * @return 是否成功踢出
     */
    public boolean forceLogout(String sno) {
        try {
            String ip = sessionPool.getCurrentLoginIp(sno);
            sessionPool.invalidateSession(sno);
            if (ip != null) {
                logger.info("强制用户[{}]从IP[{}]下线", sno, ip);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("强制用户[{}]下线时发生异常", sno, e);
            return false;
        }
    }
    
    /**
     * 验证会话是否有效
     * @param sno 学号
     * @param sessionId 会话ID
     * @return 是否有效
     */
    public boolean isValidSession(String sno, String sessionId) {
        try {
            logger.debug("验证会话 - 学号: {}, 会话ID: {}", sno, sessionId);
            boolean isValid = sessionPool.isValidSession(sno, sessionId);
            if (isValid) {
                sessionPool.updateSessionActivity(sno);
                logger.debug("会话验证成功 - 学号: {}", sno);
            } else {
                logger.debug("会话验证失败 - 学号: {}", sno);
            }
            return isValid;
        } catch (Exception e) {
            logger.error("验证会话[{}]是否有效时发生异常", sessionId, e);
            return false;
        }
    }
    
    /**
     * 获取用户当前登录的IP
     * @param sno 学号
     * @return IP地址，如果未登录则返回null
     */
    public String getCurrentLoginIp(String sno) {
        return sessionPool.getCurrentLoginIp(sno);
    }
    
    /**
     * 定时清理过期会话
     */
    @Scheduled(fixedDelay = 60000)  // 每分钟执行一次
    public void cleanExpiredSessions() {
        sessionPool.maintain();  // 使用SessionPool的maintain方法进行维护
    }
} 