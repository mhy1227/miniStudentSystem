# 登录功能问题记录

## 问题一：更新最后登录时间失败

### 问题描述
- 现象：登录时报数据库错误
- 时间：2024-04-05
- 影响：登录功能无法正常使用，无法记录用户最后登录时间

### 错误信息
```
Error updating database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'last_login_time' in 'field list'
The error may exist in com/czj/student/mapper/LoginMapper.java (best guess)
The error may involve com.czj.student.mapper.LoginMapper.updateLastLoginTime-Inline
The error occurred while setting parameters
SQL: UPDATE student SET last_login_time = NOW() WHERE sno = ?
Cause: java.sql.SQLSyntaxErrorException: Unknown column 'last_login_time' in 'field list'
```

### 原因分析
1. `LoginMapper.java` 中使用了 `last_login_time` 字段进行更新操作
2. 数据库表 `student` 中缺少该字段
3. 实体类 `Student.java` 中已有该字段定义，但数据库结构未同步更新

### 解决方案
1. 创建数据库更新脚本 `V2__add_last_login_time.sql`
2. 添加 `last_login_time` 字段：
```sql
ALTER TABLE student 
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间' AFTER login_error_count;
```

### 解决步骤
1. 创建并执行数据库更新脚本
2. 提交代码变更
3. 重启Tomcat服务器
4. 验证登录功能

### 预防措施
1. 数据库表结构变更时，确保：
   - 创建对应的SQL变更脚本
   - 实体类同步更新
   - 在测试环境验证
2. 代码提交前进行完整的功能测试
3. 保持实体类和数据库表结构的一致性

### 相关文件
- `src/main/java/com/czj/student/mapper/LoginMapper.java`
- `src/main/java/com/czj/student/model/entity/Student.java`
- `src/main/resources/db/update/V2__add_last_login_time.sql`

## 问题二：登录功能错误提示和锁定逻辑问题

### 问题描述
- 现象1：前端页面显示"登录请求失败"而不是具体的错误信息（如"密码错误，还剩2次机会"、"验证码错误"等）
- 现象2：账号锁定逻辑执行顺序不合理，导致无法正常登录
- 时间：2024-04-05
- 影响：
  1. 用户体验差，无法得知具体的错误原因
  2. 账号锁定机制可能过于严格，影响正常使用

### 错误信息
前端显示：
```
登录请求失败，请稍后重试
```

后端日志：
```
java.lang.RuntimeException: 账号已锁定，请15分钟后再试
    at com.czj.student.service.impl.LoginServiceImpl.login(LoginServiceImpl.java:32)
```

### 原因分析
1. 前端错误提示问题：
   - AJAX请求的错误处理逻辑不完整
   - 没有正确解析和显示后端返回的错误信息
   - 使用了默认的错误提示文本

2. 账号锁定逻辑问题：
   - 登录逻辑执行顺序不合理
   - 需要在验证密码前检查账号是否锁定
   - 密码正确时应该重置错误次数和锁定状态

### 问题代码
1. 前端错误处理代码：
```javascript
error: function(xhr) {
    showError('登录请求失败，请稍后重试');
}
```

2. 后端登录逻辑：
```java
public LoginUserVO login(LoginVO loginVO, HttpSession session) {
    // 1. 查询用户信息
    Student student = loginMapper.getStudentBySno(loginVO.getSno());
    if (student == null) {
        throw new RuntimeException("学号不存在");
    }

    // 2. 验证密码
    if (!student.getPwd().equals(loginVO.getPwd())) {
        // 检查账号是否已锁定
        if (isAccountLocked(loginVO.getSno(), session)) {
            throw new RuntimeException("账号已锁定，请15分钟后再试");
        }
        // ... 错误处理逻辑
    }
}
```

### 解决方案
1. 前端改进：
```javascript
error: function(xhr) {
    const response = xhr.responseJSON;
    showError(response ? response.message : '登录请求失败，请稍后重试');
    // 请求失败时刷新验证码
    generateCaptcha();
    $('#captcha').val('');
    // 清空密码输入框
    $('#pwd').val('');
}
```

2. 后端改进：
```java
public LoginUserVO login(LoginVO loginVO, HttpSession session) {
    // 1. 检查账号是否锁定
    if (isAccountLocked(loginVO.getSno(), session)) {
        throw new RuntimeException("账号已锁定，请15分钟后再试");
    }

    // 2. 查询用户信息
    Student student = loginMapper.getStudentBySno(loginVO.getSno());
    if (student == null) {
        throw new RuntimeException("学号不存在");
    }

    // 3. 验证密码
    if (!student.getPwd().equals(loginVO.getPwd())) {
        // 更新错误次数和处理锁定
        handleLoginError(student, session);
        return null;
    }

    // 4. 登录成功，重置错误记录
    resetLoginError(student, session);
    return buildLoginUserVO(student, session);
}
```

### 解决步骤
1. 修改前端错误处理逻辑：
   - 正确解析后端返回的错误信息
   - 优化错误提示显示
   - 添加验证码刷新逻辑

2. 修改后端登录逻辑：
   - 调整账号锁定检查的位置
   - 优化错误处理和成功处理的逻辑
   - 完善错误信息的返回

3. 测试验证：
   - 测试各种错误场景的提示
   - 验证账号锁定功能
   - 检查错误次数统计

### 改进效果
1. 用户可以看到具体的错误原因：
   - 密码错误剩余尝试次数
   - 验证码错误提示
   - 账号锁定状态及解锁时间

2. 账号锁定机制更合理：
   - 先检查锁定状态
   - 正确处理错误次数
   - 及时更新锁定状态

### 相关文件
- `src/main/webapp/login.html`
- `src/main/java/com/czj/student/service/impl/LoginServiceImpl.java`
- `src/main/java/com/czj/student/controller/LoginController.java`

### 预防措施
1. 前端开发规范：
   - 统一错误处理机制
   - 完善错误提示信息
   - 做好用户体验设计

2. 后端开发规范：
   - 合理设计业务逻辑流程
   - 统一异常处理
   - 详细的错误信息返回

3. 测试要求：
   - 完整测试各种错误场景
   - 验证错误提示的准确性
   - 检查安全机制的有效性

## 问题三：密码错误提示不显示剩余尝试次数

### 问题描述
- 现象：输入错误密码时，前端页面显示500错误页面，而不是友好的错误提示
- 时间：2024-04-05 至 2024-04-06
- 影响：用户体验差，无法看到剩余的尝试次数
- 日志显示：后端正确抛出了包含剩余次数的异常信息，但前端未正确显示

### 问题分析
1. 后端日志显示正确：
```
ERROR com.czj.student.aspect.LogAspect - 方法异常: 密码错误，还剩2次机会
```

2. 前端显示错误页面：
```
HTTP状态 500 - 内部服务器错误
Request processing failed; nested exception is java.lang.RuntimeException: 密码错误，还剩2次机会
```

3. 问题定位：
   - 全局异常处理器（GlobalExceptionHandler）未被Spring正确加载
   - 导致RuntimeException未被转换为ApiResponse格式
   - 最终被Tomcat默认的错误页面处理器捕获并显示500页面

### 原因分析
1. 配置问题：
   - `spring-mvc.xml` 中缺少对 `com.czj.student.common` 包的扫描配置
   - 导致 `@RestControllerAdvice` 注解的 `GlobalExceptionHandler` 未被识别
   - Spring MVC 无法使用自定义的异常处理器

2. 异常处理流程：
```
LoginServiceImpl抛出异常
    → LogAspect记录异常
    → 未被GlobalExceptionHandler处理
    → 被Tomcat默认错误页面处理
    → 显示500错误页面
```

### 解决方案
1. 修改 `spring-mvc.xml`，添加common包扫描：
```xml
<!-- 扫描包 -->
<context:component-scan base-package="com.czj.student.controller"/>
<context:component-scan base-package="com.czj.student.service"/>
<context:component-scan base-package="com.czj.student.config"/>
<context:component-scan base-package="com.czj.student.aspect"/>
<context:component-scan base-package="com.czj.student.common"/>
```

2. 确保 `GlobalExceptionHandler` 正确配置：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return ApiResponse.error(e.getMessage());
    }
}
```

3. 前端错误处理逻辑：
```javascript
error: function(xhr) {
    const response = xhr.responseJSON;
    if (response && response.message) {
        showError(response.message);
    } else {
        showError('登录请求失败，请稍后重试');
    }
}
```

### 解决效果
1. 后端异常处理：
   - RuntimeException被正确捕获并转换为ApiResponse
   - 错误信息被正确封装在response.message中

2. 前端显示效果：
   - 错误信息直接显示在登录框上方
   - 清晰显示剩余尝试次数
   - 密码错误和账号锁定的提示会持续显示
   - 验证码错误等提示会在3秒后自动消失

### 验证方法
1. 输入错误密码：显示"密码错误，还剩2次机会"
2. 再次输入错误：显示"密码错误，还剩1次机会"
3. 第三次错误：显示"账号已锁定，请1分钟后再试"

### 相关文件
- `src/main/resources/spring-mvc.xml`
- `src/main/java/com/czj/student/common/GlobalExceptionHandler.java`
- `src/main/webapp/login.html`
- `src/main/java/com/czj/student/service/impl/LoginServiceImpl.java`

### 经验总结
1. Spring组件扫描配置的重要性：
   - 确保所有需要的包都在扫描范围内
   - 特别注意通用组件所在的包

2. 异常处理最佳实践：
   - 使用全局异常处理器统一处理异常
   - 将异常转换为统一的响应格式
   - 前端统一处理错误响应

3. 调试技巧：
   - 查看后端日志定位异常
   - 使用浏览器开发者工具查看网络请求
   - 分析异常处理链路

4. 用户体验考虑：
   - 提供友好的错误提示
   - 区分不同类型的错误显示时间
   - 保持提示信息的一致性

### 预防措施
1. 开发阶段：
   - 仔细检查Spring配置文件
   - 测试异常处理流程
   - 编写完整的错误处理代码

2. 测试阶段：
   - 测试所有可能的错误场景
   - 验证错误提示的准确性
   - 检查用户体验

3. 维护阶段：
   - 定期检查错误日志
   - 收集用户反馈
   - 持续优化错误提示 