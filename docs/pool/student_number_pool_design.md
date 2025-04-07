# 学号池设计方案

## 一、需求分析

### 1. 背景
在学生管理系统中，学号是学生的唯一标识符。传统的学号生成方式通常涉及频繁的数据库操作，特别是在高并发注册场景下，可能导致数据库性能瓶颈和系统响应缓慢。

### 2. 问题定义
- 频繁的数据库操作导致性能下降
- 学号资源浪费（如用户注册后未激活或长期不活跃）
- 分布式环境下学号生成的一致性挑战
- 系统重启后学号状态的恢复问题

### 3. 目标
- 减少数据库操作，提高系统响应速度
- 实现学号的高效生成和预分配
- 确保学号生成的格式一致性
- 支持持久化和分布式部署

## 二、设计思路

### 1. 学号池的本质
学号池本质上是一个学号生成和管理机制，主要为了：

1. **减少数据库操作**：预先生成或批量加载学号，避免频繁访问数据库
2. **高效分配**：快速为新注册学生分配唯一学号
3. **生成规则统一**：确保学号格式一致（如XH000001、XH000002格式）

### 2. 核心组件
- **学号信息容器**：存储预生成的学号及其状态
- **空闲学号队列**：管理待分配的学号
- **学号生成器**：按照规则生成新学号
- **分配状态管理**：记录学号的分配状态

### 3. 数据结构选择
- 使用`ConcurrentHashMap`存储学号信息，保证线程安全
- 使用`ConcurrentLinkedQueue`管理空闲学号，支持高并发操作
- 使用`AtomicInteger`跟踪最大学号编号，保证原子性

## 三、详细设计

### 1. 学号信息类
```java
private static class SnoInfo {
    private final String sno;            // 学号
    private String userId;               // 关联的用户ID
    private boolean allocated;           // 是否已分配
    private Date allocateTime;           // 分配时间
    private boolean active;              // 是否激活
    
    // 构造器和方法...
}
```

### 2. 学号池类
```java
public class SnoPool {
    // 核心数据结构
    private final Map<String, SnoInfo> snoMap = new ConcurrentHashMap<>();  // 学号映射
    private final Queue<String> idleSnos = new ConcurrentLinkedQueue<>();   // 空闲学号队列
    private final AtomicInteger maxId = new AtomicInteger(0);               // 最大编号
    private final Lock snoLock = new ReentrantLock();                      // 锁，用于生成新学号
    
    // 统计信息
    private final AtomicInteger createdCount = new AtomicInteger(0);       // 创建总数
    private final AtomicInteger allocatedCount = new AtomicInteger(0);     // 分配总数
    private final AtomicInteger recoveredCount = new AtomicInteger(0);     // 回收总数
    
    // 配置参数
    private final int maxPoolSize;                                        // 最大池大小
    private final int preloadSize;                                        // 预加载大小
    
    // 更多成员和方法...
}
```

### 3. 核心方法

#### 预加载学号
```java
public void preloadNumbers(int count) {
    snoLock.lock();
    try {
        for (int i = 0; i < count; i++) {
            String sno = generateNewSno();
            SnoInfo info = new SnoInfo(sno, null, false, null, false);
            snoMap.put(sno, info);
            idleSnos.offer(sno);
            createdCount.incrementAndGet();
        }
    } finally {
        snoLock.unlock();
    }
}
```

#### 分配学号
```java
public String allocateNumber(String userId) {
    // 1. 检查用户是否已分配学号
    String existingSno = findSnoByUserId(userId);
    if (existingSno != null) {
        return existingSno;  // 返回已分配的学号
    }
    
    // 2. 尝试从空闲池获取
    String sno = idleSnos.poll();
    if (sno == null) {
        // 空闲池为空，预加载一批学号
        preloadNumbers(preloadSize);
        sno = idleSnos.poll();
        if (sno == null) {
            // 仍然无法获取，直接生成一个新学号
            snoLock.lock();
            try {
                sno = generateNewSno();
                createdCount.incrementAndGet();
            } finally {
                snoLock.unlock();
            }
        }
    }
    
    // 3. 更新学号信息
    SnoInfo info = snoMap.get(sno);
    if (info == null) {
        info = new SnoInfo(sno, userId, true, new Date(), false);
    } else {
        info.setUserId(userId);
        info.setAllocated(true);
        info.setAllocateTime(new Date());
    }
    snoMap.put(sno, info);
    allocatedCount.incrementAndGet();
    
    return sno;
}
```

#### 生成新学号
```java
private String generateNewSno() {
    int id = maxId.incrementAndGet();
    if (id < 10) return "S00" + id;
    else if (id < 100) return "S0" + id;
    else if (id < 1000) return "S" + id;
    return "S" + id;
}
```

#### 回收未激活学号
```java
@Scheduled(fixedRate = 86400000)  // 每天执行一次
public void recoverInactiveNumbers() {
    long now = System.currentTimeMillis();
    long recoveryThreshold = 7 * 24 * 60 * 60 * 1000; // 7天未激活就回收
    
    // 检查所有已分配但未激活的学号
    for (Map.Entry<String, SnoInfo> entry : snoMap.entrySet()) {
        SnoInfo info = entry.getValue();
        
        if (info.isAllocated() && !info.isActive() && 
            info.getAllocateTime() != null && 
            (now - info.getAllocateTime().getTime() > recoveryThreshold)) {
            
            // 重置学号状态
            info.setUserId(null);
            info.setAllocated(false);
            info.setAllocateTime(null);
            
            // 如果空闲池未满，将学号添加回空闲池
            if (idleSnos.size() < maxPoolSize) {
                idleSnos.offer(entry.getKey());
                recoveredCount.incrementAndGet();
            }
        }
    }
}
```

### 4. 学号池状态
```java
public SnoPoolStats getStats() {
    return new SnoPoolStats(
        createdCount.get(),
        allocatedCount.get(),
        recoveredCount.get(),
        idleSnos.size(),
        snoMap.size() - idleSnos.size()
    );
}

public static class SnoPoolStats {
    private final int createdCount;     // 创建的学号总数
    private final int allocatedCount;   // 已分配的学号总数
    private final int recoveredCount;   // 回收的学号总数
    private final int idleCount;        // 当前空闲学号数
    private final int allocatedCount;   // 当前已分配学号数
    
    // 构造器和方法...
}
```

## 四、与Redis结合方案

### 1. Redis存储结构
- **学号信息**：使用Redis Hash，key为"sno:info"，field为学号，value为序列化的SnoInfo对象
- **空闲学号**：使用Redis List，key为"sno:idle"，value为空闲学号
- **最大编号**：使用Redis String，key为"sno:maxId"，value为当前最大编号
- **用户学号映射**：使用Redis Hash，key为"sno:userMap"，field为用户ID，value为学号
- **统计信息**：使用Redis Hash，key为"sno:stats"，包含各种统计数据

### 2. Redis操作封装

#### 分配学号
```java
public String allocateNumberWithRedis(String userId) {
    // 1. 检查用户是否已分配学号
    String existingSno = jedis.hget("sno:userMap", userId);
    if (existingSno != null) {
        return existingSno;  // 返回已分配的学号
    }
    
    // 2. 尝试从空闲池获取
    String sno = jedis.lpop("sno:idle");
    if (sno == null) {
        // 空闲池为空，生成新学号
        long newId = jedis.incr("sno:maxId");
        sno = generateSnoFromId(newId);
        jedis.hincrBy("sno:stats", "createdCount", 1);
    } else {
        jedis.hincrBy("sno:stats", "reusedCount", 1);
    }
    
    // 3. 更新学号信息
    Map<String, String> snoInfo = new HashMap<>();
    snoInfo.put("userId", userId);
    snoInfo.put("allocated", "true");
    snoInfo.put("allocateTime", String.valueOf(System.currentTimeMillis()));
    snoInfo.put("active", "false");
    
    jedis.hmset("sno:info:" + sno, snoInfo);
    jedis.hset("sno:userMap", userId, sno);
    jedis.hincrBy("sno:stats", "allocatedCount", 1);
    
    return sno;
}
```

#### 回收未激活学号
```java
public void recoverInactiveNumbersWithRedis() {
    long now = System.currentTimeMillis();
    long recoveryThreshold = 7 * 24 * 60 * 60 * 1000; // 7天
    
    // 获取所有学号信息
    Set<String> allSnoKeys = jedis.keys("sno:info:*");
    
    for (String key : allSnoKeys) {
        Map<String, String> snoInfo = jedis.hgetAll(key);
        
        if ("true".equals(snoInfo.get("allocated")) && 
            "false".equals(snoInfo.get("active"))) {
            
            long allocateTime = Long.parseLong(snoInfo.get("allocateTime"));
            if (now - allocateTime > recoveryThreshold) {
                // 提取学号
                String sno = key.substring("sno:info:".length());
                
                // 从用户映射中移除
                String userId = snoInfo.get("userId");
                jedis.hdel("sno:userMap", userId);
                
                // 重置学号信息
                jedis.hdel("sno:info:" + sno, "userId", "allocated", "allocateTime", "active");
                
                // 添加到空闲池
                jedis.rpush("sno:idle", sno);
                jedis.hincrBy("sno:stats", "recoveredCount", 1);
            }
        }
    }
}
```

### 3. 混合存储策略
为了提高性能并确保数据一致性，可以采用混合存储策略：

1. **本地缓存**：
   - 维护一个内存中的学号池作为一级缓存
   - 适用于高频操作和快速响应

2. **Redis存储**：
   - 作为二级存储和持久化层
   - 定期同步内存缓存和Redis数据
   - 支持分布式环境下的学号分配一致性

3. **同步策略**：
   - 定期批量同步：每隔一定时间将内存更改批量同步到Redis
   - 关键操作实时同步：学号分配等关键操作立即同步
   - 启动时初始化：系统启动时从Redis加载状态到内存

## 五、与现有系统集成

### 1. 注册流程集成
学号池主要集成到学生注册流程中：

1. **注册环节**：
   - 学生提交注册信息
   - 系统从学号池分配学号
   - 创建学生账号并关联学号

2. **激活环节**：
   - 学生激活账号
   - 标记学号为已激活状态
   - 防止学号被回收

3. **注册失败或未激活**：
   - 定期检查未激活的学号
   - 达到一定期限后回收未激活学号
   - 将回收的学号放回空闲池

### 2. 接口定义
```java
// 学号池接口
public interface ISnoPool {
    // 预加载一批学号
    void preloadNumbers(int count);
    
    // 为用户分配学号
    String allocateNumber(String userId);
    
    // 标记学号为已激活
    void activateNumber(String sno);
    
    // 检查学号是否存在
    boolean numberExists(String sno);
    
    // 获取学号统计信息
    SnoPoolStats getStats();
    
    // 关闭池
    void shutdown();
}
```

## 六、性能优化与监控

### 1. 性能优化策略
- **批量预加载**：系统启动时预加载一批学号到内存
- **动态调整**：根据注册量动态调整预加载数量
- **异步加载**：当池中学号数量低于阈值时异步加载新学号
- **本地缓存**：热点数据保留在内存中

### 2. 监控指标
- **学号分配率**：每秒分配学号数
- **回收率**：未激活学号回收率
- **预加载频率**：系统自动预加载学号的频率
- **池大小**：当前可用学号数量
- **分配成功率**：学号分配成功的比率

### 3. 告警机制
- **池容量告警**：可用学号数量低于阈值时告警
- **分配失败告警**：无法分配学号时告警
- **性能下降告警**：分配耗时超过阈值时告警

## 七、风险与应对策略

### 1. 潜在风险
- **内存消耗**：预加载过多学号导致内存压力
- **数据不一致**：内存与Redis数据不同步
- **性能瓶颈**：高并发下的锁竞争
- **学号枯竭**：可用学号被耗尽

### 2. 应对措施
- **动态预加载**：根据系统负载动态调整预加载数量
- **定期同步**：建立内存和Redis的定期同步机制
- **锁优化**：最小化锁范围，考虑无锁算法
- **学号扩展**：在学号格式支持范围内，动态扩展学号生成规则

## 八、学号池与登录系统的关系

### 1. 概念区分

需要明确区分学号池与登录会话管理是两个不同的概念：

1. **学号池**：
   - 主要用于注册阶段，高效分配学生学号
   - 学号是学生的固定标识符，一旦分配不会轻易改变
   - 学号回收仅针对注册未激活的情况

2. **会话池（SessionPool）**：
   - 用于管理用户登录会话
   - 会话是临时的，代表用户的登录状态
   - 会话可以创建和销毁，不影响学生学号

### 2. 交互方式

学号池与登录系统的交互主要体现在：

1. **用户身份验证**：
   - 登录时使用学号作为用户名
   - 系统根据学号查找对应用户信息
   - 验证密码和其他信息

2. **异地登录检测**：
   - 登录系统维护学号与当前会话的映射
   - 检测同一学号是否有多个活跃会话
   - 根据策略决定是否允许多处登录

3. **数据访问权限**：
   - 学号用于确定用户对数据的访问权限
   - 登录会话用于维护当前访问状态

### 3. 实现差异

这两种池化机制的实现有本质差异：

```java
// 学号池主要关注学号的分配和管理
public class SnoPool {
    // 学号与用户的关联
    private Map<String, String> snoToUserMap;
    // 待分配的学号队列
    private Queue<String> availableSnoQueue;
    
    // 分配学号给新用户
    public String allocate(String userId) {
        // 实现分配逻辑
    }
}

// 会话池主要关注会话的创建和管理
public class SessionPool {
    // 学号与会话的关联
    private Map<String, String> snoToSessionIdMap;
    // 空闲会话对象队列
    private Queue<Session> idleSessionQueue;
    
    // 为用户创建或获取会话
    public Session borrow(String sno) {
        // 实现会话获取逻辑
    }
}
```

### 4. 结合使用示例

在完整的系统中，学号池和会话池可以结合使用：

```
1. 用户注册时：
   - 系统从学号池分配新学号
   - 创建用户账号并关联此学号
   - 用户完成激活流程

2. 用户登录时：
   - 用户提供学号和密码
   - 系统验证身份信息
   - 从会话池获取会话对象
   - 建立学号与会话的映射

3. 异地登录检测：
   - 当同一学号尝试在其他地方登录时
   - 系统检测到学号已有关联的活跃会话
   - 根据策略决定是否允许新的登录
```

此种设计使得学号管理和会话管理各司其职，同时又能协同工作，提高系统整体效率。

## 九、总结与后续规划

### 1. 方案总结
学号池设计通过高效的内存管理和可选的Redis持久化，实现了学号的快速分配和管理，有效减少了数据库操作，提高了系统性能，特别适合高并发的学生注册场景。

### 2. 后续规划
- **完善Redis集成**：实现完整的Redis持久化方案
- **添加监控功能**：开发学号池监控控制台
- **优化分配算法**：研究更高效的学号分配策略
- **支持自定义规则**：允许配置自定义的学号生成规则
- **数据分析**：添加学号使用情况的统计分析功能 