# 登录功能设计方案

## 一、数据库设计
### 1. 表结构修改
在现有的student表中添加以下字段：
```sql
ALTER TABLE student 
ADD COLUMN password VARCHAR(100) NOT NULL COMMENT '登录密码' AFTER sfzh,
ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用' AFTER password,
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间' AFTER status,
ADD COLUMN login_count INT DEFAULT 0 COMMENT '登录次数' AFTER last_login_time;
```

### 2. 字段说明
- password：存储加密后的密码
- status：账号状态，1表示启用，0表示禁用
- last_login_time：记录最后一次登录时间
- login_count：记录总登录次数

## 二、安全考虑
### 1. 密码安全
- 使用加密算法存储密码（BCrypt/MD5）
- 初始密码为身份证后6位
- 首次登录强制修改密码

### 2. 访问安全
- 防止SQL注入攻击
- 限制登录失败次数
- Session超时管理
- 密码重试次数限制

## 三、功能设计
### 1. 核心接口
- 登录接口
- 退出接口
- 修改密码
- 重置密码
- 获取当前登录用户信息

### 2. 业务规则
- 连续失败3次锁定账号
- 30分钟内未操作自动退出
- 同一账号不允许多处登录
- 密码必须包含字母和数字

### 3. 响应封装设计
#### 3.1 双层响应封装
使用双层响应封装，实现HTTP协议规范和业务响应统一：

1. 内层封装（ApiResponse）：
```java
public class ApiResponse<T> {
    private int code;        // 业务状态码
    private String message;  // 业务消息
    private T data;         // 业务数据
}
```

2. 外层封装（ResponseEntity）：
```java
ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ApiResponse.error("错误信息"))
```

#### 3.2 状态码对应关系
| 场景 | HTTP状态码 | 业务状态码 | 说明 |
|-----|------------|-----------|------|
| 成功 | 200 | 200 | 请求成功 |
| 参数错误 | 400 | 400 | 请求参数验证失败 |
| 未授权 | 401 | 401 | 未登录或会话过期 |
| 权限不足 | 403 | 403 | 无权访问 |
| 业务异常 | 500 | 500 | 如密码错误、账号锁定等 |
| 系统异常 | 500 | 500 | 未知的系统错误 |

#### 3.3 响应示例
1. 登录成功：
```json
HTTP/1.1 200 OK
{
    "code": 200,
    "message": "success",
    "data": {
        "sno": "XH000001",
        "name": "张三"
    }
}
```

2. 密码错误：
```json
HTTP/1.1 500 Internal Server Error
{
    "code": 500,
    "message": "密码错误，还剩2次机会",
    "data": null
}
```

3. 参数错误：
```json
HTTP/1.1 400 Bad Request
{
    "code": 400,
    "message": "参数校验失败：学号不能为空",
    "data": null
}
```

#### 3.4 前端处理
```javascript
$.ajax({
    success: function(response) {
        // 只处理成功响应
        window.location.replace('index.html');
    },
    error: function(xhr) {
        // 处理错误响应
        const response = xhr.responseJSON;
        if (response && response.message) {
            showError(response.message);
        }
    }
});
```

## 四、前端设计
### 1. 页面规划
- 登录页面
  - 用户名输入框（学号）
  - 密码输入框
  - 验证码（预留）
  - 记住密码选项
  - 登录按钮
  - 忘记密码链接

- 修改密码页面
  - 原密码输入
  - 新密码输入
  - 确认新密码

### 2. 交互设计
- 登录失败提示
- 密码强度提示
- 登录状态保持
- 会话超时提示

## 五、技术选型（待定）
### 1. 认证方案
- Session认证
- Token认证（JWT）
- OAuth2支持

### 2. 安全框架
- Spring Security
- Shiro
- 自定义实现

## 六、开发计划
### 第一阶段：基础登录功能
- [ ] 修改数据库表结构
- [ ] 实现基础登录接口
- [ ] 开发登录页面
- [ ] 会话管理实现

### 第二阶段：安全加固
- [ ] 添加密码加密
- [ ] 实现登录限制
- [ ] 添加验证码
- [ ] 完善安全策略

### 第三阶段：功能完善
- [ ] 实现修改密码
- [ ] 实现重置密码
- [ ] 记住密码功能
- [ ] 登录日志记录

## 七、注意事项
1. 安全性
   - 密码传输加密
   - 防止暴力破解
   - 日志脱敏处理
   
2. 性能
   - 控制Session数量
   - 合理设置超时时间
   - 优化登录查询

3. 用户体验
   - 友好的错误提示
   - 合适的密码策略
   - 便捷的找回方式

## 八、简化版登录方案（学习项目版本）

### 1. 数据库设计
在student表中添加必要的登录字段：
```sql
ALTER TABLE student 
ADD COLUMN password VARCHAR(100) NOT NULL COMMENT '登录密码' AFTER sfzh,
ADD COLUMN login_error_count INT DEFAULT 0 COMMENT '登录错误次数' AFTER password;
```

### 2. 功能范围
#### 需要实现
- 基础登录（学号+密码）
- 基础退出
- 简单的会话管理
- 登录错误次数限制（连续错误3次后锁定15分钟）

#### 暂不考虑
- 验证码
- 记住密码
- 密码重置
- 多处登录限制

### 3. 技术方案
- 认证方式：Session认证（最简单的实现）
- 不使用额外的安全框架
- 密码存储使用MD5加密
- 使用Session存储登录错误次数和锁定时间

### 4. 页面设计
简单的登录页面，包含：
- 学号输入框
- 密码输入框
- 登录按钮
- 错误提示信息（包括剩余尝试次数、账号锁定状态）
- 使用Bootstrap保持与现有系统风格一致

### 5. 整合方案
- 添加登录拦截器
- 未登录时重定向到登录页
- 登录成功后重定向到主页
- 登录失败时更新错误次数
- 超过错误次数限制时临时锁定账号

### 6. 开发计划
1. 数据库修改
   - 添加password字段
   - 添加login_error_count字段
   - 添加测试数据

2. 后端开发
   - 创建登录相关Controller
   - 实现登录拦截器
   - 添加会话管理
   - 实现登录错误次数控制逻辑
   - 实现账号临时锁定功能

3. 前端开发
   - 创建登录页面
   - 实现页面跳转
   - 添加表单验证
   - 添加错误次数和锁定状态的提示 