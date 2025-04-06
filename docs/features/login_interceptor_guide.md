# 登录拦截器实现方案

## 一、功能概述
登录拦截器用于实现系统的访问控制，确保只有已登录用户才能访问受保护的资源。

## 二、实现方案

### 1. 拦截器类设计
```java
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 实现登录检查逻辑
    }
}
```

### 2. 拦截规则
#### 2.1 白名单路径（直接放行）
- `/login.html`：登录页面
- `/api/auth/login`：登录接口
- `/api/auth/logout`：退出接口
- `/js/**`：静态JS文件
- `/css/**`：静态CSS文件
- `/images/**`：静态图片资源
- `/favicon.ico`：网站图标

#### 2.2 受保护资源（需登录访问）
- `/api/**`：所有API接口
- `/*.html`：所有HTML页面（除登录页外）
- 其他所有资源

### 3. 登录检查逻辑
1. 获取当前请求路径
2. 检查是否为白名单路径
3. 获取Session中的用户信息
4. 根据请求类型返回不同结果：
   - AJAX请求：返回JSON格式错误信息
   - 普通请求：重定向到登录页

### 4. 响应处理
#### 4.1 AJAX请求未登录响应
```json
{
    "code": 401,
    "message": "未登录或会话已过期",
    "data": null
}
```

#### 4.2 普通请求未登录处理
```
重定向到：/login.html
```

## 三、配置方案

### 1. Spring MVC配置
```xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/**"/>
        <bean class="com.czj.student.interceptor.LoginInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```

### 2. 拦截器注册
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
               .addPathPatterns("/**")
               .excludePathPatterns("/login.html", "/api/auth/login", "/js/**", "/css/**");
    }
}
```

## 四、实现细节

### 1. 请求类型判断
```java
private boolean isAjaxRequest(HttpServletRequest request) {
    String header = request.getHeader("X-Requested-With");
    return "XMLHttpRequest".equals(header);
}
```

### 2. 白名单路径判断
```java
private boolean isWhiteListUrl(String url) {
    return url.contains("/login.html") ||
           url.contains("/api/auth/login") ||
           url.contains("/js/") ||
           url.contains("/css/") ||
           url.contains("/images/") ||
           url.contains("/favicon.ico");
}
```

### 3. 未登录响应处理
```java
private void handleNotLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (isAjaxRequest(request)) {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或会话已过期\"}");
    } else {
        response.sendRedirect("/login.html");
    }
}
```

## 五、注意事项

### 1. 性能考虑
- 白名单路径使用常量存储
- 避免重复的字符串操作
- 减少Session访问次数

### 2. 安全考虑
- 防止重定向漏洞
- 清理敏感信息
- 设置安全响应头

### 3. 用户体验
- AJAX请求返回标准格式
- 友好的错误提示
- 保存当前页面URL

## 六、测试用例

### 1. 白名单测试
| 测试项 | 测试URL | 预期结果 |
|-------|---------|---------|
| 登录页面 | /login.html | 允许访问 |
| 登录接口 | /api/auth/login | 允许访问 |
| 静态资源 | /js/jquery.min.js | 允许访问 |

### 2. 拦截测试
| 测试项 | 测试URL | 预期结果 |
|-------|---------|---------|
| 普通页面 | /index.html | 重定向到登录页 |
| AJAX请求 | /api/student/list | 返回401 JSON |
| 静态资源 | /upload/file.pdf | 重定向到登录页 |

### 3. 登录状态测试
| 测试项 | 场景 | 预期结果 |
|-------|------|---------|
| 已登录 | 访问任意URL | 正常访问 |
| 会话过期 | Session超时 | 重定向或401 |
| 重新登录 | 登录后访问 | 正常访问 | 