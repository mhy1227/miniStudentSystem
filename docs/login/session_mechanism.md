# Session机制详解

## 一、两种Session机制概述

### 1. Web容器Session（web.xml）
#### 1.1 基本定义
Web容器Session是由Servlet容器（如Tomcat）提供的标准HTTP会话机制，通过web.xml配置。

#### 1.2 配置示例
```xml
<session-config>
    <!-- session超时时间（分钟） -->
    <session-timeout>30</session-timeout>
    <!-- cookie配置 -->
    <cookie-config>
        <http-only>true</http-only>
        <secure>false</secure>
        <max-age>1800</max-age>
    </cookie-config>
    <!-- 跟踪模式 -->
    <tracking-mode>COOKIE</tracking-mode>
</session-config>
```

#### 1.3 工作原理
1. **会话创建**：
   - 用户首次访问时，服务器创建HttpSession对象
   - 生成唯一的JSESSIONID
   - 通过Cookie返回JSESSIONID给浏览器

2. **会话维持**：
   - 浏览器后续请求携带JSESSIONID Cookie
   - 服务器根据JSESSIONID识别用户
   - 在会话范围内共享数据

3. **会话过期**：
   - 超过30分钟未活动
   - 浏览器关闭（Cookie消失）
   - 服务器重启（除非有持久化机制）

4. **数据存储**：
   - 存储在服务器内存中
   - 可以存储任意类型的数据
   - 数据在会话范围内共享

### 2. 自定义Session（SessionManager）
#### 2.1 基本定义
SessionManager是我们自己实现的会话管理机制，专门用于处理异地登录检测。

#### 2.2 实现示例
```java
public class SessionManager {
    // 存储用户会话信息
    private static final Map<String, UserSession> sessionMap = new ConcurrentHashMap<>();
    
    // 会话超时时间（30分钟）
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;
    
    private static class UserSession {
        private final String sno;          // 学号
        private final String sessionId;    // 会话ID
        private final String ip;           // 登录IP
        private final Date loginTime;      // 登录时间
        private volatile long lastAccessTime; // 最后访问时间
    }
}
```

#### 2.3 工作原理
1. **会话创建**：
   - 用户登录时创建UserSession对象
   - 使用学号（sno）作为key
   - 记录登录IP和时间信息

2. **会话维持**：
   - 每次请求更新最后访问时间
   - 验证会话有效性
   - 检测异地登录

3. **会话过期**：
   - 超过30分钟未活动
   - 定时任务清理过期会话
   - 用户主动登出

4. **数据存储**：
   - 存储在ConcurrentHashMap中
   - 只存储必要的会话信息
   - 专注于登录状态管理

## 二、两种Session的区别

### 1. 功能定位
- **Web容器Session**：
  - 通用的会话管理机制
  - 用于整个Web应用的状态维护
  - 支持多种数据类型的存储
  - 提供标准的Session API

- **SessionManager**：
  - 专门的登录会话管理
  - 用于异地登录检测
  - 只存储登录相关信息
  - 提供定制的会话管理API

### 2. 存储内容
- **Web容器Session**：
  - 用户登录信息（LoginUserVO）
  - 错误计数（login_error_count）
  - 锁定时间（login_lock_time）
  - 其他业务数据

- **SessionManager**：
  - 学号（sno）
  - 会话ID（sessionId）
  - IP地址
  - 登录时间
  - 最后访问时间

### 3. 生命周期
- **Web容器Session**：
  - 创建：首次访问时
  - 更新：任何请求都会更新
  - 销毁：超时或显式调用invalidate()

- **SessionManager**：
  - 创建：用户登录时
  - 更新：验证会话时
  - 销毁：超时、登出或被踢出

### 4. 超时机制
- **Web容器Session**：
  - 配置在web.xml中
  - 由容器自动管理
  - 影响整个会话

- **SessionManager**：
  - 代码中配置
  - 通过定时任务清理
  - 只影响登录状态

## 三、协同工作机制

### 1. 登录流程
```
1. 用户提交登录请求
   ↓
2. LoginController处理请求
   ↓
3. SessionManager检查是否存在其他登录
   ↓
4. 验证用户名密码
   ↓
5. 创建Web容器Session并存储用户信息
   ↓
6. 创建SessionManager会话记录
   ↓
7. 返回登录成功
```

### 2. 会话验证流程
```
1. 用户发起请求
   ↓
2. LoginInterceptor拦截请求
   ↓
3. 检查Web容器Session是否有效
   ↓
4. 获取当前登录用户信息
   ↓
5. SessionManager验证会话有效性
   ↓
6. 更新最后访问时间
   ↓
7. 放行请求
```

### 3. 登出流程
```
1. 用户点击登出
   ↓
2. 获取当前登录用户信息
   ↓
3. SessionManager清除会话记录
   ↓
4. 清除Web容器Session
   ↓
5. 清除客户端Cookie
   ↓
6. 重定向到登录页
```

## 四、安全性考虑

### 1. Web容器Session安全
1. **Cookie安全**：
   - HttpOnly防止XSS攻击
   - 可配置Secure要求HTTPS
   - 设置合理的过期时间

2. **会话固定攻击防护**：
   - 登录时重新生成SessionID
   - 使用安全的SessionID生成算法

3. **并发访问控制**：
   - 容器级别的并发处理
   - 线程安全的会话访问

### 2. SessionManager安全
1. **数据安全**：
   - 使用ConcurrentHashMap保证线程安全
   - 关键字段设置为final防止修改
   - 最小化存储敏感信息

2. **访问控制**：
   - IP地址记录和验证
   - 严格的会话有效性检查
   - 异地登录实时检测

3. **清理机制**：
   - 定时清理过期会话
   - 主动登出时立即清理
   - 异常情况下的会话清理

## 五、最佳实践

### 1. 配置建议
1. **超时时间**：
   - 两种session机制使用相同的超时时间（30分钟）
   - 根据业务需求适当调整
   - 考虑用户体验和安全性平衡

2. **Cookie设置**：
   - 启用HttpOnly
   - 生产环境启用Secure
   - 设置合适的Domain和Path

3. **清理策略**：
   - 定期清理过期会话
   - 及时释放资源
   - 记录清理日志

### 2. 使用建议
1. **数据存储**：
   - Web容器Session存储业务数据
   - SessionManager只存储登录状态
   - 避免数据重复存储

2. **异常处理**：
   - 完善的异常捕获机制
   - 合适的错误提示
   - 异常情况下的会话清理

3. **日志记录**：
   - 记录关键操作日志
   - 包含必要的调试信息
   - 便于问题排查

## 六、常见问题解决

### 1. 会话同步问题
- **现象**：两种session状态不一致
- **解决**：
  1. 登录时同步创建
  2. 登出时同步清理
  3. 定期检查并同步状态

### 2. 性能优化
- **内存使用**：
  1. 及时清理过期会话
  2. 控制session数据大小
  3. 监控内存使用情况

- **并发处理**：
  1. 使用ConcurrentHashMap
  2. 避免同步块
  3. 减少锁粒度

### 3. 异常处理
- **网络异常**：
  1. 设置合理的超时时间
  2. 添加重试机制
  3. 提供友好的错误提示

- **服务器重启**：
  1. 实现会话持久化（可选）
  2. 优雅的重启机制
  3. 用户友好的重新登录流程 