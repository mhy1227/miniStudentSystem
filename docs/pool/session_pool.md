# Session池化方案设计

## 一、基本架构

### 1. 核心组件
```java
public class SessionPool {
    // 会话池配置
    private final SessionPoolConfig config;
    
    // 可用会话队列
    private final Queue<UserSession> idlePool;
    
    // 活跃会话Map
    private final Map<String, UserSession> activePool;
    
    // 会话创建工厂
    private final SessionFactory sessionFactory;
}

// 会话池配置类
public class SessionPoolConfig {
    private int maxTotal = 100;        // 最大会话数
    private int maxIdle = 20;          // 最大空闲会话数
    private int minIdle = 5;           // 最小空闲会话数
    private long maxWaitMillis = 5000; // 最大等待时间
    private boolean testOnBorrow = true; // 借出时检测
    private boolean testOnReturn = true; // 归还时检测
}

// 会话工厂
public class SessionFactory {
    public UserSession createSession() {
        return new UserSession();
    }
    
    public boolean validateSession(UserSession session) {
        return session != null && !session.isExpired();
    }
}
```

### 2. 会话对象增强
```java
public class UserSession {
    private String sno;          // 学号
    private String sessionId;    // 会话ID
    private String ip;          // IP地址
    private Date loginTime;     // 登录时间
    private long lastAccessTime; // 最后访问时间
    
    // 池化相关属性
    private boolean inPool;     // 是否在池中
    private long createTime;    // 创建时间
    private int useCount;       // 使用次数
    private SessionPool pool;   // 所属会话池
    
    // 重置会话状态
    void reset() {
        sno = null;
        sessionId = null;
        ip = null;
        loginTime = null;
        lastAccessTime = 0;
        useCount = 0;
    }
}
```

## 二、核心功能实现

### 1. 会话获取
```java
public class SessionPool {
    public UserSession borrowSession() throws SessionException {
        // 1. 检查是否超过最大会话数
        if (activePool.size() >= config.getMaxTotal()) {
            throw new SessionException("会话池已满");
        }
        
        // 2. 尝试从空闲池获取
        UserSession session = idlePool.poll();
        
        // 3. 如果没有空闲会话，创建新的
        if (session == null) {
            session = sessionFactory.createSession();
        }
        
        // 4. 验证会话是否可用
        if (config.isTestOnBorrow() && !sessionFactory.validateSession(session)) {
            return borrowSession(); // 递归获取新会话
        }
        
        // 5. 初始化会话
        session.setInPool(true);
        session.setCreateTime(System.currentTimeMillis());
        
        // 6. 添加到活跃池
        activePool.put(session.getSessionId(), session);
        
        return session;
    }
}
```

### 2. 会话归还
```java
public class SessionPool {
    public void returnSession(UserSession session) {
        if (session == null || !session.isInPool()) {
            return;
        }
        
        // 1. 从活跃池移除
        activePool.remove(session.getSessionId());
        
        // 2. 验证会话是否可重用
        if (config.isTestOnReturn() && !sessionFactory.validateSession(session)) {
            return; // 不可重用，直接丢弃
        }
        
        // 3. 检查是否超过最大空闲数
        if (idlePool.size() >= config.getMaxIdle()) {
            return; // 超过最大空闲数，直接丢弃
        }
        
        // 4. 重置会话状态
        session.reset();
        
        // 5. 放入空闲池
        idlePool.offer(session);
    }
}
```

### 3. 定时维护
```java
public class SessionPool {
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void maintain() {
        // 1. 确保最小空闲会话数
        while (idlePool.size() < config.getMinIdle()) {
            idlePool.offer(sessionFactory.createSession());
        }
        
        // 2. 清理过期会话
        long now = System.currentTimeMillis();
        activePool.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            if (now - session.getLastAccessTime() > SESSION_TIMEOUT) {
                session.reset();
                if (idlePool.size() < config.getMaxIdle()) {
                    idlePool.offer(session);
                }
                return true;
            }
            return false;
        });
    }
}
```

## 三、使用示例

### 1. 初始化配置
```java
@Configuration
public class SessionConfig {
    @Bean
    public SessionPool sessionPool() {
        SessionPoolConfig config = new SessionPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(20);
        config.setMinIdle(5);
        return new SessionPool(config);
    }
}
```

### 2. 实际应用
```java
@Service
public class SessionService {
    @Autowired
    private SessionPool sessionPool;
    
    public boolean login(String sno, String ip) {
        UserSession session = null;
        try {
            // 1. 获取会话
            session = sessionPool.borrowSession();
            
            // 2. 设置会话信息
            session.setSno(sno);
            session.setIp(ip);
            session.setLoginTime(new Date());
            
            return true;
        } catch (SessionException e) {
            return false;
        } finally {
            // 3. 归还会话
            if (session != null) {
                sessionPool.returnSession(session);
            }
        }
    }
}
```

## 四、性能优化

### 1. 池化参数调优
- **最大会话数**：根据系统内存和并发量设置
- **空闲会话数**：根据平均负载调整
- **等待时间**：根据业务需求设置合理的超时时间

### 2. 并发优化
```java
public class SessionPool {
    // 使用信号量控制并发
    private final Semaphore semaphore;
    
    public UserSession borrowSession() throws SessionException {
        if (!semaphore.tryAcquire(config.getMaxWaitMillis(), TimeUnit.MILLISECONDS)) {
            throw new SessionException("获取会话超时");
        }
        try {
            return doGetSession();
        } finally {
            semaphore.release();
        }
    }
}
```

### 3. 监控指标
```java
public class SessionPool {
    // 统计信息
    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AtomicInteger borrowedCount = new AtomicInteger(0);
    private final AtomicInteger returnedCount = new AtomicInteger(0);
    private final AtomicInteger discardedCount = new AtomicInteger(0);
    
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
}
```

## 五、注意事项

### 1. 内存管理
- 合理设置池大小，避免内存溢出
- 及时清理过期会话
- 监控内存使用情况

### 2. 异常处理
- 会话获取超时处理
- 会话损坏处理
- 池满时的处理策略

### 3. 性能监控
- 会话使用统计
- 池状态监控
- 性能指标收集

### 4. 使用建议
- 根据实际并发量调整池大小
- 定期检查会话状态
- 合理设置超时时间 