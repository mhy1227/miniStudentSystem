# 登录拦截器测试指南

## 一、测试环境准备
1. 确保系统正常启动
2. 清除浏览器缓存和Cookie
3. 准备一个有效的学生账号（学号和密码）

## 二、测试场景

### 1. 白名单资源访问测试
| 测试项 | 测试步骤 | 预期结果 |
|-------|---------|---------|
| 登录页面 | 直接访问 `/login.html` | 可以正常访问登录页面 |
| 登录接口 | 发送POST请求到 `/api/auth/login` | 可以正常调用登录接口 |
| 静态资源 | 访问 `/js/jquery.min.js` | 可以正常加载JS文件 |
| 静态资源 | 访问 `/css/style.css` | 可以正常加载CSS文件 |
| 静态资源 | 访问 `/images/logo.png` | 可以正常加载图片 |

### 2. 未登录状态测试
| 测试项 | 测试步骤 | 预期结果 |
|-------|---------|---------|
| 访问首页 | 直接访问 `/index.html` | 重定向到登录页面 |
| 访问功能页 | 直接访问 `/student.html` | 重定向到登录页面 |
| 访问API | 调用 `/api/students` | 返回401 JSON响应 |

### 3. 已登录状态测试
| 测试项 | 测试步骤 | 预期结果 |
|-------|---------|---------|
| 访问首页 | 登录后访问 `/index.html` | 可以正常访问 |
| 访问功能页 | 登录后访问 `/student.html` | 可以正常访问 |
| 访问API | 登录后调用 `/api/students` | 可以正常获取数据 |

### 4. 会话过期测试
| 测试项 | 测试步骤 | 预期结果 |
|-------|---------|---------|
| 页面请求 | 1. 登录系统<br>2. 等待session过期<br>3. 刷新页面 | 重定向到登录页面 |
| API请求 | 1. 登录系统<br>2. 等待session过期<br>3. 调用API | 返回401 JSON响应 |

## 三、测试工具
1. 浏览器开发者工具
   - Network面板：查看请求响应状态
   - Console面板：查看JS错误信息
   - Application面板：管理Cookie和Session

2. Postman（API测试）
   - 测试各种HTTP请求
   - 查看详细的响应信息
   - 模拟不同的请求头

## 四、测试步骤

### 1. 白名单测试
1. 打开浏览器，清除所有Cookie
2. 访问以下URL，确认都能正常访问：
   ```
   http://localhost:8080/login.html
   http://localhost:8080/js/jquery.min.js
   http://localhost:8080/css/style.css
   ```

### 2. 未登录拦截测试
1. 打开新的隐私窗口（确保未登录状态）
2. 尝试访问：
   ```
   http://localhost:8080/index.html
   http://localhost:8080/student.html
   ```
3. 使用Postman发送请求：
   ```
   GET http://localhost:8080/api/students
   ```

### 3. 登录状态测试
1. 正常登录系统
2. 访问各个功能页面
3. 使用Postman发送API请求（带上Cookie）

### 4. 会话过期测试
1. 登录系统
2. 等待session过期（默认30分钟）
3. 刷新页面或调用API
4. 观察响应结果

## 五、常见问题排查

### 1. 拦截器不生效
- 检查 `spring-mvc.xml` 中的拦截器配置
- 确认请求URL是否匹配拦截规则
- 查看服务器日志是否有错误信息

### 2. 静态资源无法访问
- 检查白名单配置是否正确
- 确认资源文件路径是否正确
- 查看浏览器控制台网络请求

### 3. API返回格式异常
- 检查请求头 `X-Requested-With`
- 确认响应内容类型设置
- 查看JSON格式是否正确

## 六、测试记录表

| 测试时间 | 测试项 | 测试结果 | 问题记录 | 解决方案 |
|---------|--------|----------|----------|----------|
| | | | | |

## 七、注意事项
1. 测试前清除浏览器缓存和Cookie
2. 使用不同的浏览器和设备测试
3. 记录所有异常情况
4. 测试完成后恢复测试数据 