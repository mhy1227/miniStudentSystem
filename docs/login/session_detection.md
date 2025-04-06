# 基于Map的异地登录检测方案

## 一、方案概述
本方案采用内存Map存储会话信息的方式，实现检测同一账号是否在其他地方登录的功能。适用于单机部署的小型系统，实现简单，易于理解和维护。

## 二、核心数据结构

### 1. 用户会话对象
```java
public class UserSession {
    private String sno;          // 学号
    private String sessionId;    // 会话ID
    private String ip;          // IP地址
    private Date loginTime;     // 登录时间
    // getter/setter方法省略
}
```

### 2. 会话管理器
```java
public class SessionManager {
    // 用户会话Map：key是学号，value是会话信息
    private static Map<String, UserSession> sessionMap = new ConcurrentHashMap<>();
    
    // 会话ID与学号的映射：key是sessionId，value是学号
    private static Map<String, String> sessionIdMap = new ConcurrentHashMap<>();
}
```

## 三、核心功能实现

### 1. 登录处理
```java
public class SessionManager {
    public LoginResult login(String sno, String sessionId, String ip) {
        // 1. 检查是否已经登录
        UserSession existSession = sessionMap.get(sno);
        
        if (existSession != null) {
            // 已经在其他地方登录
            return new LoginResult(false, "账号已在其他地方登录，上次登录IP: " + existSession.getIp());
        }
        
        // 2. 创建新会话
        UserSession newSession = new UserSession();
        newSession.setSno(sno);
        newSession.setSessionId(sessionId);
        newSession.setIp(ip);
        newSession.setLoginTime(new Date());
        
        // 3. 保存会话信息
        sessionMap.put(sno, newSession);
        sessionIdMap.put(sessionId, sno);
        
        return new LoginResult(true, "登录成功");
    }
}
```

### 2. 登出处理
```java
public class SessionManager {
    public void logout(String sessionId) {
        // 1. 获取学号
        String sno = sessionIdMap.get(sessionId);
        if (sno != null) {
            // 2. 移除会话信息
            sessionMap.remove(sno);
            sessionIdMap.remove(sessionId);
        }
    }
}
```

### 3. 会话检查
```java
public class SessionManager {
    public boolean checkSession(String sessionId) {
        // 1. 获取学号
        String sno = sessionIdMap.get(sessionId);
        if (sno == null) {
            return false;
        }
        
        // 2. 获取会话信息
        UserSession session = sessionMap.get(sno);
        if (session == null) {
            return false;
        }
        
        // 3. 验证sessionId是否匹配
        return session.getSessionId().equals(sessionId);
    }
}
```

## 四、使用示例

### 1. 登录接口
```java
@PostMapping("/login")
public ApiResponse login(@RequestParam String sno, 
                        @RequestParam String password, 
                        HttpServletRequest request) {
    // 1. 验证密码...
    
    // 2. 获取sessionId和IP
    String sessionId = request.getSession().getId();
    String ip = request.getRemoteAddr();
    
    // 3. 处理登录
    LoginResult result = SessionManager.getInstance().login(sno, sessionId, ip);
    
    if (!result.isSuccess()) {
        return ApiResponse.error(result.getMessage());
    }
    
    return ApiResponse.success();
}
```

### 2. 登录拦截器
```java
public class LoginInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String sessionId = request.getSession().getId();
        
        // 检查会话是否有效
        if (!SessionManager.getInstance().checkSession(sessionId)) {
            // 未登录或会话已失效
            response.sendRedirect("/login");
            return false;
        }
        
        return true;
    }
}
```

## 五、优化功能

### 1. 定时清理过期会话
```java
public class SessionManager {
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        sessionMap.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            // 超过30分钟未活动的会话
            return (now - session.getLoginTime().getTime()) > 1800000;
        });
    }
}
```

### 2. 会话活跃度更新
```java
public class SessionManager {
    public void updateSessionActivity(String sessionId) {
        String sno = sessionIdMap.get(sessionId);
        if (sno != null) {
            UserSession session = sessionMap.get(sno);
            if (session != null) {
                session.setLoginTime(new Date());
            }
        }
    }
}
```

## 六、注意事项

### 1. 并发处理
- 使用ConcurrentHashMap保证线程安全
- 重要操作需要添加同步锁
- 避免死锁风险

### 2. 内存管理
- 定期清理过期会话
- 控制最大会话数量
- 监控内存使用情况

### 3. 使用限制
- 仅适用于单机部署
- 不支持分布式环境
- 重启服务会丢失会话信息

### 4. 建议改进
- 可以考虑使用Redis替代内存Map
- 添加会话持久化机制
- 增加更多的会话信息（如设备信息）
- 实现会话统计和分析功能

## 七、后续扩展方向

1. **会话持久化**
   - 定期将会话信息保存到数据库
   - 服务重启时恢复会话状态
   - 实现会话备份和恢复

2. **分布式支持**
   - 使用Redis替代内存Map
   - 实现分布式会话管理
   - 保证集群环境下的会话一致性

3. **安全增强**
   - 添加会话有效期机制
   - 实现会话劫持防护
   - 增加异常登录检测

4. **监控统计**
   - 统计在线用户数量
   - 记录登录日志
   - 分析用户登录行为 