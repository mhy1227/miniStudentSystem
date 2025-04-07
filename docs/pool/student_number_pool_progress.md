# 学号池开发进度文档
## 零、git创建学号池功能分支
```
# 1. 确保main分支是最新的
git pull origin main

# 2. 创建并切换到新的学号池功能分支
git checkout -b feature/student-number-pool

# 3. 添加已有的学号池文档
git add docs/pool/student_number_pool_design.md docs/pool/student_number_pool_progress.md

# 4. 提交这些文档
git commit -m "docs: 添加学号池设计文档和开发进度计划"

# 5. 推送新分支到远程仓库
git push --set-upstream origin feature/student-number-pool
```
### 1.创建新功能分支
```
# 确保当前在main分支并且是最新状态
git checkout main
git pull origin main

# 创建并切换到新的功能分支
git checkout -b feature/student-number-pool
```
### 2.添加并提交设计文档
```
# 添加学号池设计文档和进度文档到暂存区
git add docs/pool/student_number_pool_design.md docs/pool/student_number_pool_progress.md

# 提交文档变更
git commit -m "docs: 添加学号池设计文档和进度计划"

# 推送分支到远程仓库
git push --set-upstream origin feature/student-number-pool
```
### 3.分支开发流程
```
# 每次开发前同步main分支的最新变更
git checkout main
git pull origin main
git checkout feature/student-number-pool
git merge main

# 进行开发...

# 提交变更
git add .
git commit -m "feat: 实现XX功能"
git push origin feature/student-number-pool
```
### 4.功能完成之后的合并
```
# 确保功能分支包含main的最新变更
git checkout main
git pull origin main
git checkout feature/student-number-pool
git merge main

# 解决可能的冲突并提交

# 切换到main并合并功能分支
git checkout main
git merge feature/student-number-pool
git push origin main

# 可选：删除功能分支
git branch -d feature/student-number-pool
git push origin --delete feature/student-number-pool
```

## 一、当前状态

### 1. 设计阶段
- [x] 学号池概念设计
  - [x] 设计文档完成
  - [x] 数据结构选型
  - [x] 核心算法设计
  - [x] 接口定义

### 2. 开发状态
- [x] 代码实现
  - [x] 基础框架搭建
  - [x] SnoInfo内部类定义
  - [x] 并发数据结构实现
  - [x] 学号生成与分配算法

### 3. 与SessionPool的关系
- SessionPool：已完成开发和部署的会话管理池
- SnoPool：现已完成基础实现的学号管理池
- 两者是**完全独立**的组件，解决不同的业务问题：
  - SessionPool处理用户会话管理和异地登录检测
  - SnoPool处理学号生成、分配和回收

## 二、已实现的功能模块

### 1. 核心功能
- [x] SnoPool类设计与实现
  - [x] 基础框架搭建
  - [x] SnoInfo内部类定义
  - [x] 并发数据结构实现
  - [x] 学号生成与分配算法

### 2. 学号管理
- [x] 学号分配
  - [x] 优先使用回收学号
  - [x] 自动生成新学号
  - [x] 学号唯一性保证
  - [x] 并发分配处理

- [x] 学号回收
  - [x] 学号状态标记
  - [x] 回收队列管理
  - [x] 资源清理

### 3. 持久化设计
- [ ] 数据库持久化
  - [ ] 学号信息表设计
  - [ ] 批量操作优化
  - [ ] 事务处理

- [ ] Redis集成
  - [ ] 缓存结构设计
  - [ ] 原子操作支持
  - [ ] 过期策略

## 三、已完成的接口设计

### 1. 学号分配接口
- [x] 基础分配接口
  - [x] 参数验证
  - [x] 学号分配策略
  - [x] 返回结果封装

### 2. 学号查询接口
- [x] 学号状态查询
  - [x] 按学号查询
  - [x] 批量查询支持
  - [x] 状态过滤

### 3. 管理接口
- [x] 统计信息接口
  - [x] 分配情况统计
  - [x] 使用率计算
  - [x] 状态分布分析

## 四、已实现的安全设计

### 1. 基础安全
- [x] 并发控制
  - [x] 锁机制实现
  - [x] 原子操作
  - [x] 线程安全保证

- [x] 异常处理
  - [x] 边界情况处理
  - [x] 错误恢复机制
  - [x] 日志记录

### 2. 学号安全
- [x] 学号生命周期管理
  - [x] 分配时间记录
  - [x] 状态变更追踪
  - [x] 异常状态处理

## 五、测试结果

### 1. 单元测试
- [x] SnoPool测试
  - [x] 分配功能测试
  - [x] 回收功能测试
  - [x] 并发安全测试

### 2. 功能验证
- [x] 学号生成格式验证
  - [x] 符合XH+6位数字格式 (例如：XH000001)
  - [x] 序号正确递增
- [x] 学号分配与回收
  - [x] 学号成功分配给用户
  - [x] 学号成功回收
  - [x] 回收的学号成功重用
- [x] 日志输出
  - [x] 记录分配操作
  - [x] 记录回收操作
  - [x] 提供详细的操作信息

### 3. 测试日志摘要
```
2025-04-07 20:32:18.366 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000001]分配给用户[user001]
2025-04-07 20:32:18.375 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000001]已回收
2025-04-07 20:32:18.376 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000001]分配给用户[user002]
...
2025-04-07 20:32:18.392 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000020]分配给用户[user19]
2025-04-07 20:32:18.393 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000001]分配给用户[user001]
2025-04-07 20:32:18.394 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000001]已回收
2025-04-07 20:32:18.395 [main] INFO  com.czj.student.snopool.SnoPool - 学号[XH000001]分配给用户[user001]
```

## 六、实现架构

### 1. 核心组件
- **SnoPool**：核心池化实现，管理学号的生成、分配与回收
- **SnoService**：业务服务层，提供对外服务接口
- **SnoPoolController**：REST API接口，对外暴露HTTP接口

### 2. 数据结构
- **ConcurrentHashMap**: 存储学号信息，保证线程安全
- **ConcurrentLinkedQueue**: 管理空闲学号队列
- **AtomicInteger**: 管理学号序号，保证原子性
- **ReentrantLock**: 保护关键操作的线程安全

### 3. 接口设计
- **/api/sno-pool/assign**: 分配学号
- **/api/sno-pool/recycle**: 回收学号
- **/api/sno-pool/status**: 查询学号状态
- **/api/sno-pool/pool-status**: 查询池状态
- **/api/sno-pool/check**: 检查学号可用性

## 七、下一步计划

### 1. 短期目标
- [ ] 完善单元测试
  - [ ] 测试覆盖边界条件
  - [ ] 添加性能测试
  - [ ] 添加并发测试

- [ ] 实现可视化管理界面
  - [ ] 学号分配状态查看
  - [ ] 学号池监控
  - [ ] 学号手动管理

### 2. 中期目标
- [ ] 实现持久化机制
  - [ ] 数据库存储
  - [ ] 定时序列化
  - [ ] 启动时恢复

- [ ] 性能优化
  - [ ] 批量操作支持
  - [ ] 缓存机制优化
  - [ ] 读写锁分离

### 3. 长期目标
- [ ] 分布式支持
  - [ ] Redis集成
  - [ ] 分布式锁实现
  - [ ] 跨节点同步

- [ ] 高级功能
  - [ ] 学号格式定制
  - [ ] 学号预测与预分配
  - [ ] 学号生命周期管理

## 八、项目总结

### 1. 已完成工作
- 设计并实现了核心学号池功能
- 开发了完整的服务接口和REST API
- 进行了基本功能测试和验证
- 完成了详细的设计和进度文档

### 2. 设计特点
- 保持简单实用，避免过度设计
- 优先考虑功能正确性和安全性
- 面向实际业务需求，不过度抽象
- 采用线程安全的数据结构，保证并发安全

### 3. 后续建议
- 根据实际业务量调整池大小参数
- 根据需要添加持久化机制
- 考虑与用户管理系统更紧密集成
- 建立监控机制，了解使用情况

## 九、Redis升级方案

本章节提供从内存实现升级到基于Redis的学号池方案，方便后续扩展到分布式环境。

### 1. Redis数据结构设计

| 数据项 | Redis类型 | Key | 说明 |
|------|----------|-----|-----|
| 学号信息 | Hash | sno:info:{sno} | 存储每个学号的详细信息 |
| 空闲学号队列 | List | sno:idle | 存储待分配的学号列表 |
| 最大学号序号 | String | sno:maxId | 当前已分配的最大序号 |
| 用户学号映射 | Hash | sno:userMap | 用户ID到学号的映射 |
| 统计信息 | Hash | sno:stats | 存储统计数据(创建数、分配数等) |

#### 学号信息Hash结构
```
sno:info:{学号} = {
    "userId": "用户ID",
    "allocated": "true/false",
    "allocateTime": "分配时间戳",
    "active": "true/false",  
    "recycleTime": "回收时间戳"
}
```

#### 统计信息Hash结构
```
sno:stats = {
    "createdCount": "创建总数",
    "allocatedCount": "分配总数",
    "activeCount": "激活总数",
    "recycledCount": "回收总数",
    "idleCount": "空闲数量"
}
```

### 2. 核心接口修改

#### 2.1 配置类修改
```java
@Configuration
public class SnoPoolConfig {
    @Value("${redis.host:localhost}")
    private String redisHost;
    
    @Value("${redis.port:6379}")
    private int redisPort;
    
    @Value("${redis.password:}")
    private String redisPassword;
    
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(20);
        config.setMinIdle(5);
        
        if (StringUtils.isEmpty(redisPassword)) {
            return new JedisPool(config, redisHost, redisPort);
        } else {
            return new JedisPool(config, redisHost, redisPort, 2000, redisPassword);
        }
    }
    
    @Bean
    public RedisSnoPool redisSnoPool(JedisPool jedisPool) {
        return new RedisSnoPool(jedisPool);
    }
}
```

#### 2.2 Redis实现类
```java
@Component
public class RedisSnoPool implements ISnoPool {
    private static final Logger logger = LoggerFactory.getLogger(RedisSnoPool.class);
    private static final String SNO_PREFIX = "XH";
    private static final int SNO_NUMBER_LENGTH = 6;
    
    private final JedisPool jedisPool;
    
    public RedisSnoPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        init();
    }
    
    private void init() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 初始化统计信息
            if (!jedis.exists("sno:stats")) {
                Map<String, String> stats = new HashMap<>();
                stats.put("createdCount", "0");
                stats.put("allocatedCount", "0");
                stats.put("activeCount", "0");
                stats.put("recycledCount", "0");
                stats.put("idleCount", "0");
                jedis.hmset("sno:stats", stats);
            }
            
            // 初始化最大ID
            if (!jedis.exists("sno:maxId")) {
                jedis.set("sno:maxId", "0");
            }
        }
    }
    
    @Override
    public String allocateSno(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 1. 检查用户是否已有学号
            String existingSno = jedis.hget("sno:userMap", userId);
            if (existingSno != null) {
                return existingSno;
            }
            
            // 2. 从空闲队列获取学号
            String sno = jedis.lpop("sno:idle");
            
            // 3. 如果没有空闲学号，生成新学号
            if (sno == null) {
                long newId = jedis.incr("sno:maxId");
                sno = formatSno(newId);
                jedis.hincrBy("sno:stats", "createdCount", 1);
            }
            
            // 4. 更新学号信息
            Map<String, String> snoInfo = new HashMap<>();
            snoInfo.put("userId", userId);
            snoInfo.put("allocated", "true");
            snoInfo.put("allocateTime", String.valueOf(System.currentTimeMillis()));
            snoInfo.put("active", "false");
            
            jedis.hmset("sno:info:" + sno, snoInfo);
            jedis.hset("sno:userMap", userId, sno);
            jedis.hincrBy("sno:stats", "allocatedCount", 1);
            
            logger.info("学号[{}]分配给用户[{}]", sno, userId);
            return sno;
        }
    }
    
    @Override
    public boolean recycleSno(String sno) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 1. 获取学号信息
            Map<String, String> snoInfo = jedis.hgetAll("sno:info:" + sno);
            
            if (snoInfo.isEmpty() || !"true".equals(snoInfo.get("allocated"))) {
                return false;
            }
            
            // 2. 从用户映射中移除
            String userId = snoInfo.get("userId");
            jedis.hdel("sno:userMap", userId);
            
            // 3. 更新学号状态
            Map<String, String> updatedInfo = new HashMap<>();
            updatedInfo.put("allocated", "false");
            updatedInfo.put("recycleTime", String.valueOf(System.currentTimeMillis()));
            jedis.hmset("sno:info:" + sno, updatedInfo);
            
            // 4. 添加到空闲队列
            jedis.rpush("sno:idle", sno);
            jedis.hincrBy("sno:stats", "recycledCount", 1);
            
            logger.info("学号[{}]已回收", sno);
            return true;
        }
    }
    
    @Override
    public boolean isSnoAllocated(String sno) {
        try (Jedis jedis = jedisPool.getResource()) {
            String allocated = jedis.hget("sno:info:" + sno, "allocated");
            return "true".equals(allocated);
        }
    }
    
    @Override
    public SnoInfo getSnoInfo(String sno) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> redisInfo = jedis.hgetAll("sno:info:" + sno);
            if (redisInfo.isEmpty()) {
                return null;
            }
            
            SnoInfo info = new SnoInfo();
            info.setSno(sno);
            info.setUserId(redisInfo.get("userId"));
            info.setAllocated("true".equals(redisInfo.get("allocated")));
            info.setActive("true".equals(redisInfo.get("active")));
            
            String allocateTimeStr = redisInfo.get("allocateTime");
            if (allocateTimeStr != null) {
                info.setAllocateTime(new Date(Long.parseLong(allocateTimeStr)));
            }
            
            String recycleTimeStr = redisInfo.get("recycleTime");
            if (recycleTimeStr != null) {
                info.setRecycleTime(Long.parseLong(recycleTimeStr));
            }
            
            return info;
        }
    }
    
    private String formatSno(long number) {
        String numberStr = String.valueOf(number);
        int paddingZeros = SNO_NUMBER_LENGTH - numberStr.length();
        
        StringBuilder sb = new StringBuilder(SNO_PREFIX);
        for (int i = 0; i < paddingZeros; i++) {
            sb.append('0');
        }
        sb.append(numberStr);
        
        return sb.toString();
    }
    
    @Override
    public String getStatus() {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> stats = jedis.hgetAll("sno:stats");
            return String.format("SnoPool{created=%s, allocated=%s, recycled=%s, idle=%s}",
                    stats.getOrDefault("createdCount", "0"),
                    stats.getOrDefault("allocatedCount", "0"),
                    stats.getOrDefault("recycledCount", "0"),
                    stats.getOrDefault("idleCount", "0"));
        }
    }
}
```

### 3. 接口适配器设计

为了平滑过渡，可以添加适配器实现同一接口：

```java
public interface ISnoPool {
    String allocateSno(String userId);
    boolean recycleSno(String sno);
    boolean isSnoAllocated(String sno);
    SnoInfo getSnoInfo(String sno);
    String getStatus();
}

// 内存实现
@Component
@Primary
@ConditionalOnProperty(name = "sno.pool.type", havingValue = "memory", matchIfMissing = true)
public class MemorySnoPool implements ISnoPool {
    // 现有实现...
}

// Redis实现
@Component
@ConditionalOnProperty(name = "sno.pool.type", havingValue = "redis")
public class RedisSnoPool implements ISnoPool {
    // Redis实现...
}

// 服务层使用接口，不关心具体实现
@Service
public class SnoService {
    @Resource
    private ISnoPool snoPool;
    
    // 服务方法使用接口调用，不需要修改
}
```

### 4. 迁移策略

#### 4.1 配置文件修改
在application.properties中添加:
```properties
# 学号池类型: memory或redis
sno.pool.type=memory

# Redis配置
redis.host=localhost
redis.port=6379
redis.password=
```

#### 4.2 数据迁移工具
```java
@Component
public class SnoPoolMigrationTool {
    @Resource
    private MemorySnoPool memorySnoPool;
    
    @Resource
    private JedisPool jedisPool;
    
    public void migrateToRedis() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 1. 迁移统计信息
            String status = memorySnoPool.getStatus();
            // 解析状态字符串，填充Redis统计数据
            
            // 2. 迁移最大ID
            // 从内存池获取maxId并设置到Redis
            
            // 3. 迁移学号信息
            // 获取所有学号信息并保存到Redis
            
            // 4. 迁移空闲队列
            // 获取空闲学号并添加到Redis List中
        }
    }
}
```

#### 4.3 迁移脚本
```java
@Component
public class MigrationRunner implements CommandLineRunner {
    @Resource
    private SnoPoolMigrationTool migrationTool;
    
    @Value("${sno.pool.migration:false}")
    private boolean needMigration;
    
    @Override
    public void run(String... args) {
        if (needMigration) {
            migrationTool.migrateToRedis();
            System.out.println("学号池数据迁移完成!");
        }
    }
}
```

### 5. 部署与切换步骤

1. **添加Redis依赖**
   ```xml
   <dependency>
       <groupId>redis.clients</groupId>
       <artifactId>jedis</artifactId>
       <version>3.7.0</version>
   </dependency>
   ```

2. **实现Redis版本接口**
   - 添加RedisSnoPool实现类
   - 添加配置类和条件注解

3. **配置与测试**
   - 先以memory模式运行并测试
   - 设置迁移标志并运行一次迁移
   - 切换到redis模式并验证功能

4. **正式切换**
   - 备份数据
   - 设置`sno.pool.type=redis`
   - 重启应用

### 6. 性能优化建议

1. **连接池优化**
   - 根据并发量调整连接池大小
   - 使用Lettuce或Redisson等更高性能的客户端

2. **批量操作**
   - 使用Redis Pipeline减少网络往返
   - 批量预加载和迁移学号

3. **本地缓存**
   - 添加短时间的本地缓存减少Redis请求
   - 使用Caffeine等高性能缓存库

4. **监控与告警**
   - 添加Redis操作的性能监控
   - 设置连接池告警阈值 