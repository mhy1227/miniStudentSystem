# 分页功能设计文档

## 一、功能需求

### 1.1 基本需求
- 提供通用的分页查询功能，支持各种业务实体的分页
- 实现查询结果缓存，提高查询效率
- 确保数据更新时缓存自动失效
- 支持基本的排序和条件查询

### 1.2 性能需求
- 支持高并发查询
- 降低对数据库的查询压力
- 保证数据一致性

### 1.3 可扩展性需求
- 支持未来切换为Redis缓存
- 适配不同的业务实体
- 便于集成到现有和未来的功能模块

## 二、整体架构

### 2.1 核心组件
```
 +----------------+     +----------------+     +----------------+
 |                |     |                |     |                |
 |  Controller    |---->|    Service     |---->|      DAO       |
 |                |     |                |     |                |
 +----------------+     +----------------+     +----------------+
                            ^       ^
                            |       |
                   +--------+       +--------+
                   |                         |
        +----------v----------+   +----------v----------+
        |                     |   |                     |
        |   QueryAspect       |   |   UpdateAspect      |
        |                     |   |                     |
        +----------+----------+   +----------+----------+
                   |                         |
                   +----------v-------------+
                                |
                   +------------v-----------+
                   |                        |
                   |      QueryPool         |
                   |                        |
                   +------------------------+
```

### 2.2 数据流
1. 控制器接收带分页参数的请求
2. 查询切面拦截服务层方法
3. 切面检查缓存是否存在匹配的数据
4. 如果有缓存命中，直接返回；否则执行原查询方法并缓存结果
5. 更新切面拦截数据修改操作，清除相关缓存

## 三、核心组件设计

### 3.1 PageInfo 类
已存在，用于封装分页参数和结果。

```java
@Data
public class PageInfo<T> implements Serializable {
    private Integer page;      // 当前页码
    private Integer size;      // 每页记录数
    private Integer pages;     // 总页数
    private Integer total;     // 总记录数
    private List<T> rows;      // 当前页数据
    private String uuid;       // 缓存标识
    // 其他字段...
}
```

### 3.2 QueryPool 类
管理查询结果缓存的池。

```java
public class QueryPool {
    // 缓存数据，键为查询标识，值为查询结果
    private static final ConcurrentHashMap<String, List<?>> QUERY_CACHE = new ConcurrentHashMap<>();
    
    // 方法签名映射，键为方法信息，值为查询标识
    private static final ConcurrentHashMap<String, String> METHOD_CACHE = new ConcurrentHashMap<>();
    
    // 最大缓存条目数
    private static final int MAX_CACHE_SIZE = 1000;
    
    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRATION = 30 * 60 * 1000; // 30分钟
    
    // 缓存时间戳，用于自动过期
    private static final ConcurrentHashMap<String, Long> CACHE_TIMESTAMPS = new ConcurrentHashMap<>();
    
    // 其他方法...
}
```

### 3.3 QueryAspect 类
处理查询方法的AOP切面。

```java
@Aspect
@Component
public class QueryAspect {
    // 分页查询切点
    @Pointcut("@annotation(com.czj.student.annotation.PageQuery)")
    public void pageQueryPointcut() {}
    
    // 普通查询切点
    @Pointcut("@annotation(com.czj.student.annotation.CacheQuery)")
    public void cacheQueryPointcut() {}
    
    // 环绕通知，处理分页查询
    @Around("pageQueryPointcut()")
    public Object handlePageQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现分页查询逻辑...
    }
    
    // 环绕通知，处理普通缓存查询
    @Around("cacheQueryPointcut()")
    public Object handleCacheQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现普通查询缓存逻辑...
    }
}
```

### 3.4 UpdateAspect 类
处理数据更新的AOP切面。

```java
@Aspect
@Component
public class UpdateAspect {
    // 数据修改切点
    @Pointcut("@annotation(com.czj.student.annotation.CacheInvalidate)")
    public void updatePointcut() {}
    
    // 环绕通知，处理数据更新
    @Around("updatePointcut()")
    public Object handleUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        // 实现缓存失效逻辑...
    }
}
```

### 3.5 注解设计

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PageQuery {
    String[] cacheGroups() default {};  // 缓存组，用于批量失效
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheQuery {
    String[] cacheGroups() default {};  // 缓存组
    long expiration() default 1800000;  // 缓存过期时间(毫秒)，默认30分钟
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheInvalidate {
    String[] cacheGroups();  // 要失效的缓存组
}
```

## 四、关键流程

### 4.1 分页查询流程
1. 控制器接收带分页参数的请求
2. 调用服务层带@PageQuery注解的方法
3. QueryAspect拦截请求，生成缓存键
4. 检查QueryPool中是否有对应缓存
5. 如果有，直接返回缓存数据并填充PageInfo
6. 如果没有，调用原方法执行查询
7. 将查询结果保存到QueryPool
8. 返回查询结果

### 4.2 缓存失效流程
1. 数据更新方法被调用
2. UpdateAspect拦截请求
3. 清除指定缓存组的所有缓存
4. 执行原更新方法
5. 返回更新结果

### 4.3 缓存自动过期
1. 定时任务定期扫描缓存时间戳
2. 移除过期的缓存条目
3. 保持缓存大小在限制范围内，移除最不常用的条目

## 五、扩展设计

### 5.1 Redis支持（未来扩展）
```java
public interface CacheProvider {
    void put(String key, Object value, long expiration);
    <T> T get(String key, Class<T> type);
    void remove(String key);
    void removeByPattern(String pattern);
}

public class MemoryCacheProvider implements CacheProvider {
    // 内存实现...
}

public class RedisCacheProvider implements CacheProvider {
    // Redis实现...
}
```

### 5.2 缓存监控
提供缓存命中率、大小和清除次数等监控指标。

```java
public class CacheStats {
    private AtomicLong hits = new AtomicLong(0);
    private AtomicLong misses = new AtomicLong(0);
    private AtomicLong evictions = new AtomicLong(0);
    // 其他统计字段...
}
```

## 六、实现计划

### 第一阶段：基础实现
1. 创建QueryPool类，实现基本的缓存管理
2. 设计并实现三个基本注解
3. 实现QueryAspect的基本功能
4. 实现UpdateAspect的基本功能
5. 编写简单的测试服务验证功能

### 第二阶段：功能完善
1. 实现缓存自动过期机制
2. 添加缓存容量控制
3. 实现缓存组功能
4. 完善异常处理
5. 添加日志记录

### 第三阶段：性能优化与监控
1. 实现缓存统计功能
2. 优化缓存键生成策略
3. 添加JMX监控接口
4. 支持手动清除缓存的管理接口

### 第四阶段：扩展与集成
1. 设计CacheProvider接口
2. 实现MemoryCacheProvider
3. 为将来的Redis实现做准备
4. 与现有系统的深度集成

## 七、风险与规避方案

### 7.1 内存占用过大
- **风险**：缓存量过大导致内存溢出
- **规避**：设置缓存上限，实现LRU淘汰策略

### 7.2 缓存一致性问题
- **风险**：数据更新后缓存未及时失效
- **规避**：完善UpdateAspect，确保关联缓存全部失效

### 7.3 高并发下的性能问题
- **风险**：锁竞争导致性能下降
- **规避**：使用ConcurrentHashMap，减少锁粒度

### 7.4 缓存穿透
- **风险**：大量查询不存在的数据
- **规避**：对空结果也进行缓存，设置较短过期时间

## 八、与现有系统的集成点

### 8.1 学生信息管理
集成到学生信息查询功能，实现学生列表分页查询。

### 8.2 课程管理
集成到课程信息查询功能，实现课程列表分页查询。

### 8.3 成绩管理
集成到成绩查询功能，实现学生成绩分页查询。

### 8.4 与现有池的关系
- 会话池：管理用户会话
- 学号池：管理学号资源
- 查询池：管理查询结果缓存
三者相互独立，各自管理不同资源。

## 九、时间与资源估计

### 9.1 开发时间
- 第一阶段：3天
- 第二阶段：2天
- 第三阶段：2天
- 第四阶段：3天
总计：10个工作日

### 9.2 测试时间
- 单元测试：2天
- 集成测试：2天
- 性能测试：1天
总计：5个工作日

### 9.3 资源需求
- 开发人员：1名
- 测试人员：1名（兼职）
- 部署环境：现有环境即可

## 十、附录

### 10.1 参考资料
- Spring AOP文档
- 缓存设计模式
- MyBatis分页插件

### 10.2 术语解释
- **缓存命中**：在缓存中找到所需数据
- **缓存失效**：移除缓存中的数据
- **LRU**：Least Recently Used，最近最少使用策略
- **AOP**：Aspect-Oriented Programming，面向切面编程 