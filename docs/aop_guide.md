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