# SnoPool学号池增强实现方案

## 1. 概述

本文档详细描述了对现有SnoPool学号池组件的增强计划，主要聚焦于超时回收机制的优化、持久化支持和监控功能的增强。

## 2. 需求背景

当前SnoPool实现存在以下限制：

- 学号资源纯内存管理，系统重启后状态丢失
- 超时回收机制较为简单，仅支持单一超时策略
- 缺乏完整的监控和告警机制
- 分布式环境下不支持协同工作

## 3. 实现目标

1. **增强超时回收机制**：
   - 实现多级超时策略
   - 优化回收算法，提高效率
   - 增加手动触发回收的API

2. **添加持久化支持**：
   - 实现学号分配状态的持久化存储
   - 支持系统重启后的状态恢复
   - 提供数据一致性保障

3. **完善监控功能**：
   - 增加详细的操作日志
   - 实现关键指标的统计和暴露
   - 设计监控告警接口

## 4. 技术方案

### 4.1 多级超时策略

创建超时策略枚举和配置类：

```java
public enum SnoTimeoutStrategy {
    REGISTRATION(30 * 60 * 1000L),      // 注册场景 - 30分钟
    ADMIN_ALLOCATION(24 * 60 * 60 * 1000L), // 管理员分配 - 24小时
    BATCH_IMPORT(7 * 24 * 60 * 60 * 1000L); // 批量导入 - 7天
    
    private final long timeout;
    
    SnoTimeoutStrategy(long timeout) {
        this.timeout = timeout;
    }
    
    public long getTimeout() {
        return timeout;
    }
}
```

修改SnoInfo类，支持超时策略：

```java
public class SnoInfo {
    // 现有字段...
    private SnoTimeoutStrategy timeoutStrategy;
    private boolean persistent; // 是否为持久分配
    
    // 构造方法和getter/setter...
}
```

### 4.2 持久化支持

创建持久化存储接口：

```java
public interface SnoAllocationRepository {
    void save(SnoAllocation allocation);
    void deleteBySno(String sno);
    List<SnoAllocation> findAll();
    Optional<SnoAllocation> findBySno(String sno);
}
```

持久化实体类：

```java
public class SnoAllocation {
    private String sno;
    private String userId;
    private long allocateTime;
    private SnoTimeoutStrategy strategy;
    private boolean persistent;
    
    // 构造方法和getter/setter...
}
```

实现基于文件的简单持久化：

```java
@Component
public class FileSnoAllocationRepository implements SnoAllocationRepository {
    private static final String STORAGE_FILE = "sno_allocations.dat";
    
    // 实现方法...
}
```

### 4.3 监控增强

创建监控统计类：

```java
public class SnoPoolStats {
    private final AtomicLong totalAllocations = new AtomicLong(0);
    private final AtomicLong currentAllocations = new AtomicLong(0);
    private final AtomicLong recycledCount = new AtomicLong(0);
    private final AtomicLong timeoutRecycledCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> lastOperationTime = new ConcurrentHashMap<>();
    
    // 方法实现...
}
```

在SnoPool中集成：

```java
@Component
public class EnhancedSnoPool {
    // 其他字段...
    private final SnoPoolStats stats = new SnoPoolStats();
    private final SnoAllocationRepository repository;
    
    // 实现方法...
}
```

## 5. API设计

### 5.1 增强的分配方法

```java
/**
 * 分配学号，支持指定超时策略
 */
public String allocateSno(String userId, SnoTimeoutStrategy strategy)

/**
 * 永久分配学号（不会被超时回收）
 */
public String allocatePermanentSno(String userId)
```

### 5.2 增强的回收方法

```java
/**
 * 回收指定学号
 */
public boolean recycleSno(String sno)

/**
 * 强制触发超时回收
 */
public int forceTimeoutRecycle()

/**
 * 回收指定用户的所有学号
 */
public int recycleUserSno(String userId)
```

### 5.3 监控API

```java
/**
 * 获取学号池统计信息
 */
public SnoPoolStats getStats()

/**
 * 获取学号池状态报告（包含详细信息）
 */
public SnoPoolStatusReport getStatusReport()
```

## 6. 实现步骤

1. 创建基础实体和接口
2. 实现多级超时策略
3. 开发持久化组件
4. 增强SnoPool核心逻辑
5. 实现监控统计功能
6. 开发REST API接口
7. 单元测试和集成测试
8. 文档编写

## 7. 项目计划

| 阶段 | 内容 | 计划时间 |
|-----|------|---------|
| 设计 | 详细设计和接口规范定义 | 1天 |
| 开发 | 核心功能实现 | 3天 |
| 测试 | 单元测试和集成测试 | 1天 |
| 文档 | 使用文档和API文档 | 1天 |

## 8. 风险评估

1. **兼容性风险**：需确保与现有代码的兼容性
2. **性能风险**：持久化可能带来额外开销
3. **测试覆盖风险**：超时回收机制的测试需要特殊处理

## 9. 附录

### 9.1 相关类图

(此处应包含类图)

### 9.2 配置示例

```properties
# SnoPool配置
snopool.timeout.default=1800000
snopool.persistence.enabled=true
snopool.persistence.path=./data
snopool.monitoring.enabled=true
``` 