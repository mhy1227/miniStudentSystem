# Redis缓存升级详细步骤

## 思考角度
我需要提供详细的步骤，说明如何将当前内存缓存升级到Redis缓存。基于之前的文档和分析，这需要涉及几个方面的修改：
1. 依赖添加和配置
2. Redis连接配置
3. QueryPool类的修改
4. 缓存键生成策略调整
5. 序列化/反序列化处理
6. 过期时间设置
7. 缓存组管理方式变更
8. 测试和迁移策略
重要的是要保持API兼容性，让业务代码不需要修改，只是替换底层实现。

将当前基于内存的缓存机制升级到Redis分布式缓存，需要进行以下修改。所有更改应遵循"对使用者透明"的原则，保持API兼容性，使业务代码无需修改。

## 1.依赖添加
```xml
<!-- pom.xml 添加Redis依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>${spring-boot.version}</version>
</dependency>

<!-- 添加连接池支持 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.11.1</version>
</dependency>

<!-- 添加JSON序列化支持 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.14.2</version>
</dependency>
```
## 2.Redis配置添加
创建配置类：
`src/main/java/com/czj/student/config/RedisConfig.java：`
```java
package com.czj.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 创建JSON序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        
        // 设置key和value的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```
添加Redis配置到 application.properties：
```yaml
# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0

# Redis连接池配置
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=0
spring.redis.lettuce.pool.max-wait=-1ms

# 缓存前缀设置
app.cache.redis.prefix=student_system:
```
## 3.创建Redis缓存服务
创建 `src/main/java/com/czj/student/util/pool/RedisService.java：`

```java
package com.czj.student.util.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.cache.redis.prefix:student_system:}")
    private String cachePrefix;
    
    // 生成带前缀的完整键名
    private String getFullKey(String key) {
        return cachePrefix + key;
    }
    
    // 存储值
    public void set(String key, Object value, long expireTime) {
        String fullKey = getFullKey(key);
        redisTemplate.opsForValue().set(fullKey, value, expireTime, TimeUnit.MILLISECONDS);
    }
    
    // 获取值
    public <T> T get(String key, Class<T> clazz) {
        String fullKey = getFullKey(key);
        Object value = redisTemplate.opsForValue().get(fullKey);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }
    
    // 检查键是否存在
    public boolean hasKey(String key) {
        String fullKey = getFullKey(key);
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
    }
    
    // 删除键
    public void delete(String key) {
        String fullKey = getFullKey(key);
        redisTemplate.delete(fullKey);
    }
    
    // 批量删除键
    public void deleteKeys(Collection<String> keys) {
        Set<String> fullKeys = keys.stream()
            .map(this::getFullKey)
            .collect(java.util.stream.Collectors.toSet());
        redisTemplate.delete(fullKeys);
    }
    
    // 将键添加到集合
    public void addToSet(String setKey, String value) {
        String fullKey = getFullKey(setKey);
        redisTemplate.opsForSet().add(fullKey, value);
    }
    
    // 从集合获取所有成员
    public Set<String> getSetMembers(String setKey) {
        String fullKey = getFullKey(setKey);
        return redisTemplate.opsForSet().members(fullKey)
            .stream()
            .map(obj -> (String) obj)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    // 设置过期时间
    public void expire(String key, long expireTime) {
        String fullKey = getFullKey(key);
        redisTemplate.expire(fullKey, expireTime, TimeUnit.MILLISECONDS);
    }
    
    // 存储列表
    public <T> void setList(String key, List<T> list, long expireTime) {
        set(key, list, expireTime);
    }
    
    // 获取列表
    public <T> List<T> getList(String key, Class<T> itemClass) {
        return (List<T>) get(key, List.class);
    }
}
```
## 4.改造 QueryPool 类
修改 `src/main/java/com/czj/student/util/pool/QueryPool.java`，将内存存储替换为Redis：
```java
package com.czj.student.util.pool;

import com.czj.student.model.vo.PageInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class QueryPool {
    private static final Logger logger = LoggerFactory.getLogger(QueryPool.class);
    
    // 缓存统计
    private static final AtomicLong hits = new AtomicLong(0);
    private static final AtomicLong misses = new AtomicLong(0);
    
    private static RedisService redisService;
    
    // Redis key 前缀
    private static final String CACHE_PREFIX = "query_cache:";
    private static final String METHOD_PREFIX = "method_mapping:";
    private static final String GROUP_PREFIX = "cache_group:";
    
    @Autowired
    public QueryPool(RedisService redisService) {
        QueryPool.redisService = redisService;
    }
    
    // 获取分页结果
    @SuppressWarnings("unchecked")
    public static <T> PageInfo<T> getPagedResult(String cacheKey, PageInfo<T> pageInfo) {
        String fullCacheKey = CACHE_PREFIX + cacheKey;
        
        if (!redisService.hasKey(fullCacheKey)) {
            misses.incrementAndGet();
            logger.debug("Cache miss for key: {}", cacheKey);
            return null;
        }
        
        List<T> cachedList = redisService.getList(fullCacheKey, Object.class);
        if (cachedList == null) {
            misses.incrementAndGet();
            return null;
        }
        
        hits.incrementAndGet();
        logger.debug("Cache hit for key: {}", cacheKey);
        
        // 创建新的PageInfo并填充数据
        PageInfo<T> result = new PageInfo<>(pageInfo.getPage(), pageInfo.getSize());
        result.setTotal(pageInfo.getTotal());
        result.setPages(pageInfo.getPages());
        result.setRows(cachedList);
        result.setUuid(cacheKey);
        
        return result;
    }
    
    // 获取列表结果
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(String cacheKey) {
        String fullCacheKey = CACHE_PREFIX + cacheKey;
        
        if (!redisService.hasKey(fullCacheKey)) {
            misses.incrementAndGet();
            return null;
        }
        
        hits.incrementAndGet();
        return redisService.getList(fullCacheKey, Object.class);
    }
    
    // 存储结果到缓存
    public static <T> void putResult(String cacheKey, List<T> result, long expiration, String[] cacheGroups) {
        String fullCacheKey = CACHE_PREFIX + cacheKey;
        
        // 存储数据
        redisService.setList(fullCacheKey, result, expiration);
        
        // 关联到缓存组
        if (cacheGroups != null && cacheGroups.length > 0) {
            for (String group : cacheGroups) {
                String groupKey = GROUP_PREFIX + group;
                redisService.addToSet(groupKey, cacheKey);
                
                // 确保缓存组也有过期时间（比数据稍长）
                redisService.expire(groupKey, expiration + 600000);
            }
        }
        
        logger.debug("Cached result with key: {}, groups: {}", cacheKey, cacheGroups);
    }
    
    // 存储方法映射
    public static void putMethodMapping(String methodKey, String cacheKey) {
        String fullMethodKey = METHOD_PREFIX + methodKey;
        redisService.set(fullMethodKey, cacheKey, 24 * 60 * 60 * 1000);
    }
    
    // 根据方法获取缓存键
    public static String getCacheKeyByMethod(String methodKey) {
        String fullMethodKey = METHOD_PREFIX + methodKey;
        return redisService.get(fullMethodKey, String.class);
    }
    
    // 生成方法键
    public static String generateMethodKey(ProceedingJoinPoint joinPoint, Object... additionalParams) {
        // 方法签名部分保持不变
        StringBuilder sb = new StringBuilder();
        sb.append(joinPoint.getSignature().getDeclaringTypeName())
          .append(".")
          .append(joinPoint.getSignature().getName());
        
        // 添加方法参数
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                if (arg instanceof PageInfo) {
                    PageInfo<?> pageInfo = (PageInfo<?>) arg;
                    sb.append(":")
                      .append(pageInfo.getPage())
                      .append(":")
                      .append(pageInfo.getSize());
                } else {
                    sb.append(":").append(arg.toString());
                }
            } else {
                sb.append(":null");
            }
        }
        
        // 添加额外参数
        for (Object param : additionalParams) {
            if (param != null) {
                sb.append(":").append(param.toString());
            } else {
                sb.append(":null");
            }
        }
        
        return sb.toString();
    }
    
    // 生成缓存键
    public static String generateCacheKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    // 清除缓存组
    public static void clearCacheGroup(String cacheGroup) {
        String groupKey = GROUP_PREFIX + cacheGroup;
        
        // 获取组内所有缓存键
        Set<String> cacheKeys = redisService.getSetMembers(groupKey);
        if (cacheKeys != null && !cacheKeys.isEmpty()) {
            // 删除所有缓存数据
            for (String cacheKey : cacheKeys) {
                redisService.delete(CACHE_PREFIX + cacheKey);
            }
            
            logger.debug("Cleared cache group: {}, keys: {}", cacheGroup, cacheKeys.size());
        }
        
        // 删除缓存组本身
        redisService.delete(groupKey);
    }
    
    // 清除所有缓存
    public static void clearAll() {
        // 需要实现自定义的清除所有相关缓存的逻辑
        // 此处可能需要管理员权限或特殊处理
        logger.warn("clearAll() method called, but not implemented for Redis");
    }
    
    // 获取缓存统计信息
    public static String getStats() {
        long hitCount = hits.get();
        long missCount = misses.get();
        long total = hitCount + missCount;
        double hitRate = total > 0 ? (double) hitCount / total * 100 : 0;
        
        return String.format("Cache Stats: hits=%d, misses=%d, hit_rate=%.2f%%", 
                            hitCount, missCount, hitRate);
    }
}
```
## 5.修改缓存切面
修改 QueryAspect 类，处理Redis相关异常：
```java
package com.czj.student.aspect;

import com.czj.student.annotation.PageQuery;
import com.czj.student.model.vo.PageInfo;
import com.czj.student.util.pool.QueryPool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QueryAspect {
    private static final Logger logger = LoggerFactory.getLogger(QueryAspect.class);
    
    @Around("@annotation(com.czj.student.annotation.PageQuery)")
    public Object handlePageQuery(ProceedingJoinPoint joinPoint, PageQuery pageQuery) throws Throwable {
        try {
            // 尝试使用Redis缓存的原有逻辑
            String methodKey = QueryPool.generateMethodKey(joinPoint);
            String cacheKey = QueryPool.getCacheKeyByMethod(methodKey);
            
            if (cacheKey != null) {
                PageInfo<Object> pageInfo = (PageInfo<Object>) joinPoint.getArgs()[0];
                PageInfo<Object> cachedResult = QueryPool.getPagedResult(cacheKey, pageInfo);
                
                if (cachedResult != null) {
                    return cachedResult;
                }
            }
            
            // 缓存未命中或键不存在，执行原方法
            Object result = joinPoint.proceed();
            
            // 存储结果到缓存
            if (result instanceof PageInfo) {
                PageInfo<?> pageInfo = (PageInfo<?>) result;
                String newCacheKey = QueryPool.generateCacheKey();
                QueryPool.putResult(newCacheKey, pageInfo.getRows(), 
                                   pageQuery.expiration(), pageQuery.cacheGroups());
                QueryPool.putMethodMapping(methodKey, newCacheKey);
            }
            
            return result;
            
        } catch (RedisConnectionFailureException e) {
            // Redis连接失败，回退到直接执行方法
            logger.warn("Redis connection failed, falling back to direct method execution", e);
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.error("Error in cache aspect", e);
            // 不应阻止业务逻辑，在缓存失败时直接执行方法
            return joinPoint.proceed();
        }
    }
}

```
## 6.创建Redis健康监测
创建 `src/main/java/com/czj/student/monitor/RedisHealthMonitor.java`：
```java
package com.czj.student.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisHealthMonitor.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private volatile boolean redisAvailable = true;
    
    public boolean isRedisAvailable() {
        return redisAvailable;
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkRedisHealth() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            
            if (!redisAvailable) {
                logger.info("Redis connection restored");
            }
            redisAvailable = true;
        } catch (RedisConnectionFailureException e) {
            if (redisAvailable) {
                logger.warn("Redis connection failed: {}", e.getMessage());
            }
            redisAvailable = false;
        } catch (Exception e) {
            logger.error("Error checking Redis health", e);
            redisAvailable = false;
        }
    }
}
```

## 7. 添加缓存迁移工具
创建 `src/main/java/com/czj/student/util/pool/CacheMigrationTool.java`：
```java
package com.czj.student.util.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存迁移工具，仅在迁移期间使用
 */
@Component
public class CacheMigrationTool implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheMigrationTool.class);
    
    @Autowired
    private RedisService redisService;
    
    // 内存缓存引用（仅用于迁移，正式切换后应删除）
    private ConcurrentHashMap<String, Object> memoryCache;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否需要迁移
        if (memoryCache != null && !memoryCache.isEmpty()) {
            logger.info("Starting cache migration from memory to Redis...");
            
            AtomicInteger count = new AtomicInteger(0);
            memoryCache.forEach((key, value) -> {
                try {
                    // 将内存缓存数据迁移到Redis
                    redisService.set(key, value, 30 * 60 * 1000); // 默认30分钟过期
                    count.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Failed to migrate cache key: " + key, e);
                }
            });
            
            logger.info("Cache migration completed. Migrated {} items.", count.get());
        }
    }
}
```

## 8.修改启动类添加Redis支持
修改 `src/main/java/com/czj/student/Application.java`：
```java
package com.czj.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching // 启用缓存
@EnableScheduling // 启用调度任务
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 9.测试与迁移策略
### 测试步骤
#### 1.单元测试Redis服务：
  创建 `src/test/java/com/czj/student/util/pool/RedisServiceTest.java`
  测试Redis连接与操作
#### 2.集成测试缓存功能：
  创建 `src/test/java/com/czj/student/util/pool/QueryPoolRedisTest.java`
  测试缓存核心功能
#### 3.性能对比测试：
  创建 `src/test/java/com/czj/student/performance/CachePerformanceTest.java`
  对比内存缓存与Redis缓存性能
### 迁移策略
#### 1.配置参数控制：
添加配置参数控制缓存模式：
```yaml
   # 缓存模式: memory, redis, dual
   app.cache.mode=dual
   ```

####   2.双模式缓存：
在 dual 模式下，同时写入内存和Redis，优先从Redis读取，Redis不可用时回退到内存缓存
####   3.灰度发布：
阶段1: 部署双模式缓存到少量服务器(20%)
阶段2: 扩展到50%服务器
阶段3: 全量部署，切换到仅Redis模式
####  4.监控告警：
添加Redis缓存相关监控指标，设置告警阈值


## 10.部署注意事项
### 1.Redis集群配置：
生产环境应使用Redis集群或主从复制确保高可用性
### 2.安全配置：
- 启用Redis身份验证
- 配置网络访问控制
- 加密Redis连接
### 3.资源规划：
评估内存需求（监控Redis内存使用）
配置合理的过期策略
设置内存上限和淘汰策略
### 4.备份策略：
设置定期RDB或AOF备份
### 5.监控与预警：
监控Redis性能指标
设置连接失败告警
监控内存使用率与缓存命中率

通过以上步骤，可以平滑地将内存缓存升级为Redis分布式缓存，在保持API兼容性的同时获得分布式缓存的优势。