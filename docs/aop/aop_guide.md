# AOP日志实现指导

## 一、基础配置
1. 添加依赖
```xml
<!-- pom.xml -->
<!-- 在properties中定义版本号 -->
<properties>
    <spring.version>5.3.20</spring.version>
    <aspectj.version>1.9.7</aspectj.version>
</properties>

<!-- Spring AOP依赖 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aop</artifactId>
    <version>${spring.version}</version>
</dependency>

<!-- AspectJ依赖 -->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectj.version}</version>
</dependency>

<!-- 如果已经有spring-webmvc依赖，可以不用添加spring-aop，因为webmvc已经包含了 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>${spring.version}</version>
</dependency>
```

注意：
- 版本号要和项目中其他Spring依赖版本保持一致
- 如果项目中已经有spring-webmvc依赖，只需要添加aspectjweaver依赖即可
- 建议使用properties统一管理版本号

2. 创建配置类
```java
// src/main/java/com/czj/student/config/AopConfig.java
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
}
```

## 二、创建日志切面
```java
// src/main/java/com/czj/student/aspect/LogAspect.java
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    // 定义切点：controller包下所有方法
    @Pointcut("execution(* com.czj.student.controller.*.*(..))")
    public void logPointcut() {}

    // 环绕通知
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        // 1. 获取开始时间
        long startTime = System.currentTimeMillis();
        
        // 2. 获取请求信息
        String className = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();
        
        // 3. 打印请求信息
        logger.info("开始调用: {}.{}", className, methodName);
        logger.info("方法参数: {}", Arrays.toString(args));
        
        // 4. 执行原方法
        Object result = null;
        try {
            result = point.proceed();
            // 5. 打印响应结果
            logger.info("方法返回: {}", result);
        } catch (Exception e) {
            // 6. 打印异常信息
            logger.error("方法异常: {}", e.getMessage());
            throw e;
        } finally {
            // 7. 打印执行时间
            long endTime = System.currentTimeMillis();
            logger.info("执行耗时: {}ms", (endTime - startTime));
        }
        
        return result;
    }
}
```

## 三、测试验证
1. 启动项目
2. 调用任意controller接口（如查询学生列表）
3. 观察控制台日志输出：
   - 是否显示请求的类名和方法名
   - 是否显示请求参数
   - 是否显示返回结果
   - 是否显示执行时间

## 四、预期日志格式
```
开始调用: com.czj.student.controller.StudentController.list
方法参数: [PageRequest{pageNum=1, pageSize=10}]
方法返回: {"code":200, "message":"success", "data":{...}}
执行耗时: 123ms
```

## 五、注意事项
1. 确保包名正确
2. 注意日志级别设置
3. 可以根据需要调整日志内容
4. 如果遇到AOP不生效，检查：
   - 配置类是否正确
   - 包扫描是否正确
   - 切点表达式是否正确 

## 六、AOP日志实现流程

### 1. 配置类
```java
// src/main/java/com/czj/student/config/AopConfig.java
@Configuration
@EnableAspectJAutoProxy  // 开启AOP功能
public class AopConfig {
}
```

### 2. 自定义注解
```java
// src/main/java/com/czj/student/annotation/Log.java
@Target(ElementType.METHOD)  // 作用在方法上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String module() default "";      // 模块名称
    String type() default "";        // 操作类型
    String description() default ""; // 描述
}
```

### 3. 切面类
```java
// src/main/java/com/czj/student/aspect/LogAspect.java
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);
    
    @Around("@annotation(com.czj.student.annotation.Log)")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        // 1. 获取开始时间
        long startTime = System.currentTimeMillis();
        
        // 2. 获取注解信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        
        // 3. 记录请求信息
        logger.info("模块名称: {}", logAnnotation.module());
        logger.info("操作类型: {}", logAnnotation.type());
        logger.info("操作描述: {}", logAnnotation.description());
        logger.info("方法参数: {}", Arrays.toString(point.getArgs()));
        
        // 4. 执行目标方法
        Object result = null;
        try {
            result = point.proceed();
            logger.info("执行结果: {}", result);
        } catch (Exception e) {
            logger.error("执行异常: {}", e.getMessage());
            throw e;
        } finally {
            logger.info("执行耗时: {}ms", System.currentTimeMillis() - startTime);
        }
        
        return result;
    }
}
```

### 4. Controller中使用注解
```java
// src/main/java/com/czj/student/controller/StudentController.java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    @Log(module = "学生管理", type = "查询", description = "分页查询学生列表")
    @GetMapping
    public ApiResponse<PageResult<Student>> list(Student student, PageRequest pageRequest) {
        // 业务逻辑
    }
    
    @Log(module = "学生管理", type = "新增", description = "新增学生信息")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody Student student) {
        // 业务逻辑
    }
}
```

### 5. 包扫描配置
```xml
<!-- spring-mvc.xml -->
<context:component-scan base-package="com.czj.student.aspect"/>
<context:component-scan base-package="com.czj.student.controller"/>
```

或使用注解配置：
```java
@Configuration
@ComponentScan({"com.czj.student.aspect", "com.czj.student.controller"})
public class WebConfig {
}
```

### 6. 日志配置
```xml
<!-- logback.xml -->
<configuration>
    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>app.log</file>
        <encoder>
            <charset>UTF-8</charset>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <charset>UTF-8</charset>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### 实现流程总结
1. **配置类**
   - 添加AOP依赖
   - 创建配置类
   - 开启AOP功能

2. **自定义注解**
   - 创建自定义注解类
   - 设置注解属性
   - 指定注解的作用范围

3. **切面类**
   - 创建切面类
   - 定义切点
   - 实现通知方法
   - 处理日志记录逻辑

4. **使用注解**
   - 在Controller方法上添加注解
   - 配置注解参数
   - 添加业务描述信息

5. **包扫描配置**
   - 配置组件扫描
   - 包含切面和控制器包
   - 确保注解和切面被识别

6. **日志配置**
   - 配置日志文件
   - 设置日志格式
   - 指定日志级别
   - 配置输出方式

### 注意事项
1. 确保所有必要的依赖都已添加
2. 注意包扫描的配置是否正确
3. 检查日志配置的编码设置
4. 合理设置日志级别
5. 定期检查日志文件大小
6. 考虑添加日志轮转策略 

## 七、个人理解版本的AOP实现流程

### 简单来说，实现AOP日志功能就是这么几步：

1. **先把Config配好**
   - 就是创建一个配置类
   - 加上`@Configuration`注解
   - 再加上`@EnableAspectJAutoProxy`开启AOP功能
   - 这样Spring才知道我们要用AOP

2. **写个自定义注解**
   - 创建一个`@Log`注解
   - 里面定义几个属性：模块名称、操作类型、描述之类的
   - 这样我们想记录什么信息都可以通过注解传进去

3. **写切面（最核心的）**
   - 创建切面类`LogAspect`
   - 加上`@Aspect`和`@Component`注解
   - 实现切面方法，就是实际记录日志的地方
   - 在这里可以拿到注解的信息，记录方法执行的各种细节

4. **在Controller上用注解**
   - 在需要记录日志的方法上加`@Log`注解
   - 填写模块名称、操作类型这些信息
   - 比如`@Log(module = "学生管理", type = "查询")`

5. **别忘了包扫描**
   - 确保Spring能找到我们的切面类
   - 把aspect包和controller包都扫描到
   - 不然再好的代码Spring也找不到啊

6. **最后配置日志输出**
   - 配置logback.xml
   - 设置日志输出格式
   - 可以同时输出到控制台和文件

### 整个过程就像搭积木：
1. Config是地基 ➡️ 
2. 注解是积木 ➡️ 
3. 切面是搭建方法 ➡️ 
4. Controller使用注解就是在搭积木 ➡️ 
5. 包扫描是告诉Spring去哪找积木 ➡️ 
6. 日志配置是决定最后长啥样

### 容易出错的地方：
1. 包名写错了（最常见）
2. 忘了加注解
3. 包扫描没配好
4. 日志级别设置不对

记住：配置类 ➡️ 注解 ➡️ 切面 ➡️ 使用注解 ➡️ 包扫描 ➡️ 日志配置，这个顺序很重要！ 