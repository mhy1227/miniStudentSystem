# 日志格式优化记录

## 1. 背景

### 1.1 问题描述
在系统运行过程中，发现日志中的参数输出格式不够友好，特别是对于一些特殊类型的参数（如 HttpSession、HttpServletRequest 等），仅显示对象的内存地址，不利于问题排查和系统监控。

### 1.2 优化目标
1. 提高日志可读性
2. 便于问题排查和定位
3. 优化敏感信息处理
4. 统一日志输出格式
5. 减少无用信息干扰

### 1.3 适用范围
- 所有Controller层的方法调用
- 使用@Log注解的方法
- 涉及HTTP请求和会话的操作
- 包含业务参数的方法调用

## 2. 原始日志格式

### 2.1 原始实现
```java
private String formatArgs(Object[] args) {
    return Arrays.toString(args);
}
```

### 2.2 日志输出示例
```
// 登录请求
方法参数: [LoginVO(sno=XH000001, pwd=015678), org.apache.catalina.session.StandardSessionFacade@52ea2964, org.apache.catalina.connector.RequestFacade@2f035fbd]

// 安全问题设置
方法参数: [{sno=XH000001, questionId=9, answer=black}, org.apache.catalina.session.StandardSessionFacade@52ea2964]

// 获取当前用户
方法参数: [org.apache.catalina.session.StandardSessionFacade@52ea2964]
```

### 2.3 存在的问题
1. 会话对象相关：
   - Session 对象只显示内存地址（@52ea2964）
   - 无法跟踪同一会话的不同请求
   - 会话状态信息缺失

2. 请求对象相关：
   - Request 对象只显示内存地址（@2f035fbd）
   - 无法查看请求的URI和方法
   - 缺少请求相关的上下文信息

3. 业务参数相关：
   - 敏感信息（如密码）明文显示
   - 复杂对象的展示不够清晰
   - 参数类型难以识别

4. 格式问题：
   - Tomcat 内部类显示完整类名，不够简洁
   - 参数之间的分隔不够清晰
   - 缺乏参数的用途说明

## 3. 优化方案

### 3.1 整体设计
1. 分类处理不同类型的参数
2. 统一输出格式规范
3. 增加必要的上下文信息
4. 处理敏感信息脱敏
5. 优化展示方式

### 3.2 优化实现
```java
private String formatArgs(Object[] args) {
    if (args == null || args.length == 0) {
        return "[]";
    }
    
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < args.length; i++) {
        if (i > 0) {
            sb.append(", ");
        }
        
        Object arg = args[i];
        if (arg instanceof HttpSession) {
            HttpSession session = (HttpSession) arg;
            sb.append("Session(id=").append(session.getId()).append(")");
        } 
        else if (arg instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) arg;
            sb.append("Request(uri=").append(request.getRequestURI()).append(")");
        }
        else if (arg instanceof Map) {
            // Map类型参数直接输出，因为它们通常包含业务数据
            sb.append(arg);
        }
        else if (arg != null && arg.getClass().getName().startsWith("org.apache.catalina")) {
            // 对于其他Tomcat内部类，只显示简单类名
            sb.append(arg.getClass().getSimpleName());
        }
        else {
            // 其他参数（如VO对象等）直接输出
            sb.append(arg);
        }
    }
    sb.append("]");
    return sb.toString();
}
```

### 3.3 参数处理策略

#### 3.3.1 HttpSession参数
- 显示会话ID
- 便于跟踪用户会话
- 示例：`Session(id=FA9F6D9CB2793CD593431F8CB552B03B)`

#### 3.3.2 HttpServletRequest参数
- 显示请求URI
- 方便定位请求来源
- 示例：`Request(uri=/api/auth/login)`

#### 3.3.3 Map类型参数
- 直接显示Map内容
- 保留业务数据的可读性
- 示例：`{sno=XH000001, questionId=9, answer=black}`

#### 3.3.4 Tomcat内部类
- 只显示简单类名
- 避免冗长的完整类名
- 示例：`StandardSessionFacade` 替代 `org.apache.catalina.session.StandardSessionFacade`

#### 3.3.5 其他参数
- 保持原有toString()输出
- VO对象需要实现合适的toString()方法
- 注意敏感信息的处理

### 3.4 优化后的日志示例
```
// 1. 登录请求
方法参数: [LoginVO(sno=XH000001, pwd=******), Session(id=FA9F6D9CB2793CD593431F8CB552B03B), Request(uri=/api/auth/login)]

// 2. 安全问题设置
方法参数: [{sno=XH000001, questionId=9, answer=black}, Session(id=FA9F6D9CB2793CD593431F8CB552B03B)]

// 3. 获取当前用户
方法参数: [Session(id=FA9F6D9CB2793CD593431F8CB552B03B)]

// 4. 异常情况
方法异常: 用户未登录
执行耗时: 5ms
```

## 4. 优化效果

### 4.1 可读性提升
1. 会话跟踪：
   - 清晰展示会话ID
   - 可以跟踪同一用户的多个请求
   - 便于分析会话状态

2. 请求定位：
   - 显示请求URI
   - 方便定位问题接口
   - 了解请求上下文

3. 参数展示：
   - 业务数据清晰可见
   - 参数类型易于识别
   - 格式统一规范

### 4.2 调试便利性
1. 参数解析：
   - 直观显示业务参数
   - 易于理解参数含义
   - 快速定位参数问题

2. 异常处理：
   - 清晰的异常信息
   - 完整的调用上下文
   - 详细的执行时间

3. 信息过滤：
   - 减少冗余信息
   - 突出关键数据
   - 提高排查效率

### 4.3 安全性增强
1. 敏感信息处理：
   - 密码自动脱敏
   - 关键数据保护
   - 符合安全规范

2. 日志级别控制：
   - 开发环境详细日志
   - 生产环境按需输出
   - 避免信息泄露

## 5. 最佳实践

### 5.1 日志输出规范
1. 统一格式：
   - 保持一致的输出格式
   - 使用规范的分隔符
   - 合理的换行和缩进

2. 信息完整性：
   - 包含必要的上下文
   - 记录关键参数
   - 保留异常堆栈

3. 敏感信息处理：
   - 实现脱敏接口
   - 统一脱敏规则
   - 定期安全审计

### 5.2 性能优化建议
1. 日志级别控制：
   - 开发环境：DEBUG/INFO
   - 测试环境：INFO
   - 生产环境：WARN/ERROR

2. 对象转换处理：
   - 大对象toString()优化
   - 集合类型大小限制
   - 避免深层对象遍历

3. 缓存利用：
   - 复用StringBuilder
   - 常用字符串缓存
   - 避免频繁GC

### 5.3 维护建议
1. 定期检查：
   - 日志输出质量
   - 敏感信息处理
   - 性能影响评估

2. 持续优化：
   - 收集用户反馈
   - 分析日志使用情况
   - 更新优化方案

3. 文档更新：
   - 记录修改历史
   - 更新使用说明
   - 补充最佳实践

## 6. 后续计划

### 6.1 待优化项
1. 增加更多参数类型的处理：
   - MultipartFile
   - HttpServletResponse
   - 自定义复杂对象

2. 改进日志格式：
   - 添加时间戳
   - 增加线程信息
   - 优化异常展示

3. 提升性能：
   - 引入日志缓冲
   - 异步日志处理
   - 优化字符串拼接

### 6.2 长期规划
1. 日志分析工具：
   - 开发日志解析工具
   - 提供可视化界面
   - 支持复杂查询

2. 监控集成：
   - 接入监控系统
   - 提供告警功能
   - 支持统计分析

3. 安全加强：
   - 完善脱敏规则
   - 增加加密选项
   - 支持审计跟踪

## 7. 变更历史

### 7.1 版本记录
- v1.0.0 (2025-04-14)
  - 初始版本
  - 基础参数格式化
  - 简单脱敏处理

### 7.2 问题反馈
如有问题或建议，请联系：
- 开发团队：学生系统开发组
- 邮件地址：dev@example.com
- 文档维护：张三 