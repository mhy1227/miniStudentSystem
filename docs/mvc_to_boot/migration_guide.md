# 从SpringMVC迁移到SpringBoot指南

## 目录

1. [概述](#概述)
2. [项目结构变化](#项目结构变化)
3. [依赖管理](#依赖管理)
4. [配置文件转换](#配置文件转换)
5. [应用启动类](#应用启动类)
6. [Web配置](#Web配置)
7. [数据库配置](#数据库配置)
8. [会话管理](#会话管理)
9. [异常处理](#异常处理)
10. [日志配置](#日志配置)
11. [测试迁移](#测试迁移)
12. [部署方式变更](#部署方式变更)
13. [迁移步骤和检查清单](#迁移步骤和检查清单)

## 概述

本文档提供了将当前基于SpringMVC的学生管理系统迁移到SpringBoot框架的全面指导。迁移的主要目标是：

- 简化配置，减少XML文件的使用
- 利用SpringBoot的自动配置特性
- 提高开发效率和应用性能
- 更好地支持微服务架构
- 获得更丰富的生态系统支持

迁移过程将保持核心业务逻辑不变，主要是调整项目结构、配置方式和部署方法。

## 项目结构变化

### 当前SpringMVC项目结构

```
miniStudentSystem/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/czj/student/
│   │   │       ├── common/
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── interceptor/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       ├── session/
│   │   │       └── snopool/
│   │   ├── resources/
│   │   │   ├── logback.xml
│   │   │   ├── mybatis/
│   │   │   ├── spring-mybatis.xml
│   │   │   └── spring-mvc.xml
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       ├── js/
│   │       ├── css/
│   │       └── *.html
│   └── test/
├── pom.xml
└── docs/
```

### SpringBoot项目结构

```
miniStudentSystem/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/czj/student/
│   │   │       ├── MiniStudentSystemApplication.java  # 新增：启动类
│   │   │       ├── config/                           # 新增：配置类目录
│   │   │       ├── common/
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── interceptor/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       ├── session/
│   │   │       └── snopool/
│   │   ├── resources/
│   │   │   ├── application.yml                      # 新增：主配置文件
│   │   │   ├── application-dev.yml                  # 新增：开发环境配置
│   │   │   ├── application-prod.yml                 # 新增：生产环境配置
│   │   │   ├── static/                              # 静态资源
│   │   │   │   ├── js/
│   │   │   │   ├── css/
│   │   │   │   └── images/
│   │   │   ├── templates/                           # 模板文件（如果使用）
│   │   │   └── mybatis/
│   │   │       └── mapper/
│   └── test/
├── pom.xml                                          # 修改为SpringBoot依赖
└── docs/
```

### 主要变化

1. 移除`webapp`目录，静态资源移至`resources/static`
2. 移除XML配置文件，改用Java配置类
3. 新增SpringBoot启动类
4. 使用`application.yml`替代各种XML配置

## 依赖管理

### 当前pom.xml

```xml
<dependencies>
    <!-- Spring MVC -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>${spring.version}</version>
    </dependency>

    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>${mybatis.version}</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis-spring</artifactId>
        <version>2.0.7</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>${mysql.version}</version>
    </dependency>

    <!-- Druid -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>${druid.version}</version>
    </dependency>
    
    <!-- 其他依赖... -->
</dependencies>
```

### SpringBoot pom.xml

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.10</version>
    <relativePath/>
</parent>

<dependencies>
    <!-- SpringBoot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- SpringBoot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- MyBatis SpringBoot -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- MySQL驱动 -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Druid SpringBoot -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.8</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- AspectJ (AOP) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 主要变化

1. 引入SpringBoot父项目管理依赖版本
2. 使用Starter依赖简化配置
3. 移除显式版本号（由父项目管理）
4. 添加SpringBoot Maven插件用于打包可执行JAR

## 配置文件转换

### Spring MVC XML配置

当前项目使用多个XML文件进行配置：
- `spring-mvc.xml`: Web MVC配置
- `spring-mybatis.xml`: MyBatis和数据源配置
- `web.xml`: Servlet配置

### SpringBoot YAML配置

在SpringBoot中，我们将使用`application.yml`替代大部分XML配置：

```yaml
# application.yml
spring:
  # 应用配置
  application:
    name: mini-student-system
  
  # 数据源配置
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mini_student_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: password
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall
      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: 123456
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
  
  # 会话配置
  session:
    timeout: 30m
    cookie:
      http-only: true

# MyBatis配置
mybatis:
  mapper-locations: classpath:mybatis/mapper/*.xml
  type-aliases-package: com.czj.student.model
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true

# Web配置
server:
  port: 8080
  servlet:
    context-path: /
    session:
      timeout: 30m
      cookie:
        http-only: true

# 日志配置
logging:
  level:
    root: info
    com.czj.student: debug
  file:
    name: logs/mini-student-system.log
```

创建不同环境的配置文件：

```yaml
# application-dev.yml (开发环境)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mini_student_system_dev
    username: dev_user
    password: dev_password
  
logging:
  level:
    com.czj.student: debug
```

```yaml
# application-prod.yml (生产环境)
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/mini_student_system
    username: prod_user
    password: ${DB_PASSWORD}  # 从环境变量获取
  
logging:
  level:
    com.czj.student: info
```

### 配置类

创建Java配置类替代XML配置：

```java
// com/czj/student/config/WebConfig.java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    // Web相关配置
    @Resource
    private LoginInterceptor loginInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login.html", "/api/auth/login", "/js/**", "/css/**", "/images/**");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

```java
// com/czj/student/config/MyBatisConfig.java
@Configuration
@MapperScan("com.czj.student.dao")
public class MyBatisConfig {
    // MyBatis相关配置（如果需要）
}
```

## 应用启动类

创建一个新的主类来启动SpringBoot应用程序：

```java
// com/czj/student/MiniStudentSystemApplication.java
package com.czj.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class MiniStudentSystemApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MiniStudentSystemApplication.class, args);
    }
}
```

## Web配置

### 拦截器配置

当前项目中的拦截器需要调整为SpringBoot方式：

```java
// com/czj/student/interceptor/LoginInterceptor.java
@Component
public class LoginInterceptor implements HandlerInterceptor {
    // 现有代码保持不变
}
```

### 控制器配置

控制器的基本结构无需更改，但需要移除XML扫描配置：

```java
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    // 现有代码保持不变
}
```

## 数据库配置

### 移除XML数据源配置

移除spring-mybatis.xml中的数据源配置，改用application.yml。

### MyBatis配置

1. 添加启动类的`@MapperScan`注解
2. 移除XML中的mapper扫描配置
3. mapper文件位置配置在application.yml中

```java
// 可选：如果需要自定义MyBatis配置
@Configuration
public class MyBatisConfig {
    
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setCacheEnabled(true);
        };
    }
}
```

## 会话管理

会话池相关配置需要从web.xml迁移：

```java
@Configuration
public class SessionPoolConfig {
    
    @Bean
    public SessionPool sessionPool(
            @Value("${session.pool.max-total:100}") int maxTotal,
            @Value("${session.pool.max-idle:20}") int maxIdle,
            @Value("${session.pool.min-idle:5}") int minIdle,
            @Value("${session.pool.max-wait-millis:5000}") long maxWaitMillis,
            @Value("${session.pool.session-timeout:1800000}") long sessionTimeout) {
        
        return new SessionPool(maxTotal, maxIdle, minIdle, maxWaitMillis, sessionTimeout);
    }
}
```

在application.yml中配置：

```yaml
# 自定义会话池配置
session:
  pool:
    max-total: 100
    max-idle: 20
    min-idle: 5
    max-wait-millis: 5000
    session-timeout: 1800000
```

## 异常处理

SpringBoot中使用`@ControllerAdvice`进行统一异常处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ApiResponse handleException(Exception e) {
        return ApiResponse.error(e.getMessage());
    }
    
    @ExceptionHandler(SessionException.class)
    public ApiResponse handleSessionException(SessionException e) {
        return ApiResponse.error(401, e.getMessage());
    }
    
    // 其他异常处理...
}
```

## 日志配置

SpringBoot默认使用Logback，无需额外配置文件：

```yaml
# 在application.yml中配置
logging:
  level:
    root: info
    com.czj.student: debug
  file:
    name: logs/mini-student-system.log
```

如需高级配置，创建`logback-spring.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    
    <property name="LOG_PATH" value="logs"/>
    <property name="LOG_FILE" value="${LOG_PATH}/mini-student-system.log"/>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%date %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
        <appender-ref ref="CONSOLE"/>
    </root>
    
    <logger name="com.czj.student" level="DEBUG"/>
</configuration>
```

## 测试迁移

### 单元测试

从JUnit 4迁移到JUnit 5：

```java
// SpringMVC方式
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-mybatis.xml"})
public class SnoPoolTest {
    // 测试代码
}
```

改为：

```java
// SpringBoot方式
@SpringBootTest
class SnoPoolTest {
    // 测试代码（基本保持不变）
    
    @Test
    void testSnoAllocation() {
        // 测试代码
    }
}
```

## 部署方式变更

### 当前部署方式

当前项目部署为WAR文件，需要外部的Tomcat服务器。

### SpringBoot部署方式

SpringBoot可以打包为可执行的JAR文件，内嵌了Tomcat服务器：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

打包命令：

```bash
mvn clean package
```

运行命令：

```bash
java -jar target/mini-student-system-1.0-SNAPSHOT.jar
```

如果需要在特定环境运行：

```bash
java -jar target/mini-student-system-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### 保留WAR部署方式（可选）

如果需要保留WAR部署方式，可以：

1. 修改packaging为war
2. 添加ServletInitializer类
3. 将内嵌Tomcat依赖设为provided

```xml
<packaging>war</packaging>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

添加ServletInitializer：

```java
public class ServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MiniStudentSystemApplication.class);
    }
}
```

## 迁移步骤和检查清单

### 1. 准备工作

- [ ] 备份当前项目
- [ ] 创建新的Git分支（如`feature/spring-boot-migration`）
- [ ] 更新pom.xml，添加SpringBoot依赖

### 2. 基础结构变更

- [ ] 创建SpringBoot启动类
- [ ] 创建application.yml配置文件
- [ ] 移动静态资源到resources/static目录
- [ ] 创建Java配置类替代XML配置

### 3. 功能模块迁移

- [ ] 数据库配置迁移
- [ ] 会话管理迁移
- [ ] Web配置和拦截器迁移
- [ ] 异常处理迁移
- [ ] 日志配置迁移

### 4. 测试和验证

- [ ] 更新单元测试
- [ ] 验证核心功能
- [ ] 测试会话管理
- [ ] 检查日志输出

### 5. 打包和部署

- [ ] 配置SpringBoot Maven插件
- [ ] 测试打包和运行
- [ ] 创建不同环境的配置

### 6. 文档和交接

- [ ] 更新项目文档
- [ ] 记录迁移过程和注意事项
- [ ] 培训团队成员

## 结论

将SpringMVC项目迁移到SpringBoot将显著简化配置，提高开发效率，并为未来的扩展和微服务化奠定基础。虽然迁移工作需要一定的时间和资源投入，但长期收益是值得的。

通过遵循本文档的指导，可以实现平滑迁移，同时保持现有功能不变。迁移过程应当分步骤进行，每步都经过充分测试，确保系统的稳定性不受影响。 