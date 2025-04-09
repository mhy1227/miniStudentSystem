# SnoPool学号池增强设计规范

## 1. 组件架构

### 1.1 整体架构

SnoPool增强版将采用分层架构设计：

```
+---------------------------+
|       Controller层        |  <- REST API接口
+---------------------------+
|        Service层          |  <- 业务逻辑处理
+---------------------------+
|       Core SnoPool层      |  <- 核心功能实现
+---------------------------+
|     Repository层          |  <- 持久化存储
+---------------------------+
```

### 1.2 核心组件关系

```
                   +----------------+
                   |  SnoController |
                   +--------+-------+
                            |
                            v
                   +----------------+
                   |   SnoService   |
                   +--------+-------+
                            |
                            v
+----------------+  +----------------+  +----------------+
| SnoPoolStats   |<-| EnhancedSnoPool|->| SnoAllocation  |
+----------------+  +--------+-------+  | Repository     |
                            |           +----------------+
                            v
                   +----------------+
                   |SnoTimeoutStrategy|
                   +----------------+
```

## 2. 关键类设计

### 2.1 SnoTimeoutStrategy

```java
package com.czj.student.snopool.strategy;

/**
 * 学号超时策略枚举，定义不同场景的超时时间
 */
public enum SnoTimeoutStrategy {
    REGISTRATION(30 * 60 * 1000L, "注册场景"),
    ADMIN_ALLOCATION(24 * 60 * 60 * 1000L, "管理员分配"),
    BATCH_IMPORT(7 * 24 * 60 * 60 * 1000L, "批量导入"),
    PERMANENT(-1L, "永久分配");
    
    private final long timeout;
    private final String description;
    
    SnoTimeoutStrategy(long timeout, String description) {
        this.timeout = timeout;
        this.description = description;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isPermanent() {
        return this == PERMANENT;
    }
    
    /**
     * 根据超时时间获取最匹配的策略
     */
    public static SnoTimeoutStrategy fromTimeout(long timeout) {
        if (timeout < 0) {
            return PERMANENT;
        }
        
        SnoTimeoutStrategy bestMatch = REGISTRATION;
        long minDiff = Math.abs(timeout - REGISTRATION.getTimeout());
        
        for (SnoTimeoutStrategy strategy : values()) {
            if (strategy == PERMANENT) {
                continue;
            }
            
            long diff = Math.abs(timeout - strategy.getTimeout());
            if (diff < minDiff) {
                minDiff = diff;
                bestMatch = strategy;
            }
        }
        
        return bestMatch;
    }
}
```

### 2.2 SnoAllocation

```java
package com.czj.student.snopool.model;

import com.czj.student.snopool.strategy.SnoTimeoutStrategy;
import java.io.Serializable;

/**
 * 学号分配记录，用于持久化存储
 */
public class SnoAllocation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sno;
    private String userId;
    private long allocateTime;
    private SnoTimeoutStrategy strategy;
    private boolean persistent;
    
    // 构造函数
    public SnoAllocation() {
    }
    
    public SnoAllocation(String sno, String userId, long allocateTime, 
                         SnoTimeoutStrategy strategy, boolean persistent) {
        this.sno = sno;
        this.userId = userId;
        this.allocateTime = allocateTime;
        this.strategy = strategy;
        this.persistent = persistent;
    }
    
    // Getter和Setter方法
    // ...
    
    /**
     * 是否已过期
     */
    public boolean isExpired(long currentTime) {
        if (persistent || strategy.isPermanent()) {
            return false;
        }
        return (currentTime - allocateTime) > strategy.getTimeout();
    }
    
    @Override
    public String toString() {
        return "SnoAllocation{" +
                "sno='" + sno + '\'' +
                ", userId='" + userId + '\'' +
                ", allocateTime=" + allocateTime +
                ", strategy=" + strategy +
                ", persistent=" + persistent +
                '}';
    }
}
```

### 2.3 SnoAllocationRepository

```java
package com.czj.student.snopool.repository;

import com.czj.student.snopool.model.SnoAllocation;
import java.util.List;
import java.util.Optional;

/**
 * 学号分配持久化仓库接口
 */
public interface SnoAllocationRepository {
    /**
     * 保存学号分配记录
     */
    void save(SnoAllocation allocation);
    
    /**
     * 根据学号删除分配记录
     */
    void deleteBySno(String sno);
    
    /**
     * 查询所有学号分配记录
     */
    List<SnoAllocation> findAll();
    
    /**
     * 根据学号查询分配记录
     */
    Optional<SnoAllocation> findBySno(String sno);
    
    /**
     * 根据用户ID查询分配记录列表
     */
    List<SnoAllocation> findByUserId(String userId);
    
    /**
     * 批量保存分配记录
     */
    void saveAll(List<SnoAllocation> allocations);
    
    /**
     * 清除所有分配记录
     */
    void deleteAll();
}
```

### 2.4 SnoPoolStats

```java
package com.czj.student.snopool.stats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 学号池统计信息
 */
public class SnoPoolStats {
    // 总分配次数
    private final AtomicLong totalAllocations = new AtomicLong(0);
    
    // 当前分配数量
    private final AtomicLong currentAllocations = new AtomicLong(0);
    
    // 回收总次数
    private final AtomicLong recycledCount = new AtomicLong(0);
    
    // 超时回收次数
    private final AtomicLong timeoutRecycledCount = new AtomicLong(0);
    
    // 最后操作时间记录
    private final ConcurrentHashMap<String, Long> lastOperationTime = new ConcurrentHashMap<>();
    
    // 按策略统计的分配数量
    private final ConcurrentHashMap<String, AtomicLong> allocationsByStrategy = new ConcurrentHashMap<>();
    
    // 分配失败次数
    private final AtomicLong allocationFailures = new AtomicLong(0);
    
    // 方法实现...
    // ...
    
    /**
     * 获取统计快照
     */
    public Map<String, Object> getSnapshot() {
        Map<String, Object> snapshot = new ConcurrentHashMap<>();
        snapshot.put("totalAllocations", totalAllocations.get());
        snapshot.put("currentAllocations", currentAllocations.get());
        snapshot.put("recycledCount", recycledCount.get());
        snapshot.put("timeoutRecycledCount", timeoutRecycledCount.get());
        snapshot.put("allocationsByStrategy", new HashMap<>(allocationsByStrategy));
        snapshot.put("allocationFailures", allocationFailures.get());
        return snapshot;
    }
}
```

### 2.5 EnhancedSnoPool

增强版SnoPool将是现有SnoPool的扩展，主要增加以下功能：

1. 多级超时策略支持
2. 持久化存储集成
3. 增强的监控和统计功能
4. 分布式环境协作支持

```java
package com.czj.student.snopool;

import com.czj.student.snopool.model.SnoAllocation;
import com.czj.student.snopool.repository.SnoAllocationRepository;
import com.czj.student.snopool.stats.SnoPoolStats;
import com.czj.student.snopool.strategy.SnoTimeoutStrategy;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class EnhancedSnoPool {
    // 核心功能实现...
}
```

## 3. 接口设计

### 3.1 REST API

```
# 分配学号
POST /api/sno-pool/assign
参数:
- userId: 用户ID
- strategy: 超时策略(可选)
- persistent: 是否永久分配(可选)

# 回收学号
POST /api/sno-pool/recycle
参数:
- sno: 学号

# 强制触发超时回收
POST /api/sno-pool/force-recycle

# 查询学号状态
GET /api/sno-pool/status
参数:
- sno: 学号

# 获取池状态报告
GET /api/sno-pool/pool-status

# 回收指定用户的所有学号
POST /api/sno-pool/recycle-user
参数:
- userId: 用户ID
```

### 3.2 EnhancedSnoPool服务接口

```java
/**
 * 分配学号（默认策略）
 */
public String allocateSno(String userId);

/**
 * 分配学号（指定策略）
 */
public String allocateSno(String userId, SnoTimeoutStrategy strategy);

/**
 * 永久分配学号
 */
public String allocatePermanentSno(String userId);

/**
 * 回收学号
 */
public boolean recycleSno(String sno);

/**
 * 回收用户的所有学号
 */
public int recycleUserSno(String userId);

/**
 * 强制触发超时回收
 */
public int forceTimeoutRecycle();

/**
 * 获取统计信息
 */
public SnoPoolStats getStats();
```

## 4. 存储设计

### 4.1 文件存储格式

持久化的学号分配记录将使用JSON格式存储：

```json
[
  {
    "sno": "XH000001",
    "userId": "user001",
    "allocateTime": 1586416783000,
    "strategy": "REGISTRATION",
    "persistent": false
  },
  {
    "sno": "XH000002",
    "userId": "user002",
    "allocateTime": 1586416783000,
    "strategy": "ADMIN_ALLOCATION",
    "persistent": true
  }
]
```

### 4.2 文件存储位置

默认存储在应用数据目录下：

```
${app.data.dir}/snopool/sno_allocations.json
```

配置项：

```properties
snopool.persistence.enabled=true
snopool.persistence.path=${app.data.dir}/snopool
snopool.persistence.filename=sno_allocations.json
```

## 5. 多级超时策略详细设计

### 5.1 策略配置

配置文件中可以自定义各策略的超时时间：

```properties
# 策略超时配置（毫秒）
snopool.timeout.registration=1800000
snopool.timeout.admin=86400000
snopool.timeout.batch=604800000
```

### 5.2 策略选择逻辑

```java
/**
 * 根据场景选择合适的超时策略
 */
public SnoTimeoutStrategy selectStrategy(String scene) {
    switch (scene) {
        case "registration":
            return SnoTimeoutStrategy.REGISTRATION;
        case "admin":
            return SnoTimeoutStrategy.ADMIN_ALLOCATION;
        case "batch":
            return SnoTimeoutStrategy.BATCH_IMPORT;
        case "permanent":
            return SnoTimeoutStrategy.PERMANENT;
        default:
            return SnoTimeoutStrategy.REGISTRATION;
    }
}
```

## 6. 兼容性设计

为确保与现有系统的兼容性，EnhancedSnoPool将实现与原有SnoPool相同的核心方法，并保持相同的返回值类型。

兼容性适配器:

```java
/**
 * 兼容性适配器，保持与原有接口的兼容性
 */
@Component
public class SnoPoolCompatibilityAdapter {
    @Resource
    private EnhancedSnoPool enhancedSnoPool;
    
    /**
     * 兼容原有的allocateSno方法
     */
    public String allocateSno(String userId) {
        return enhancedSnoPool.allocateSno(userId);
    }
    
    /**
     * 兼容原有的recycleSno方法
     */
    public boolean recycleSno(String sno) {
        return enhancedSnoPool.recycleSno(sno);
    }
    
    /**
     * 兼容原有的isSnoAllocated方法
     */
    public boolean isSnoAllocated(String sno) {
        return enhancedSnoPool.isSnoAllocated(sno);
    }
    
    /**
     * 兼容原有的getSnoInfo方法
     */
    public SnoPool.SnoInfo getSnoInfo(String sno) {
        // 转换为原有的SnoInfo格式返回
        return enhancedSnoPool.getLegacySnoInfo(sno);
    }
}
```

## 7. 配置项

```properties
# 基础配置
snopool.max-total=1000
snopool.idle-timeout=1800000

# 持久化配置
snopool.persistence.enabled=true
snopool.persistence.path=./data/snopool
snopool.persistence.auto-save=true
snopool.persistence.save-interval=300000

# 超时策略配置
snopool.timeout.registration=1800000
snopool.timeout.admin=86400000
snopool.timeout.batch=604800000

# 监控配置
snopool.monitoring.enabled=true
snopool.monitoring.log-interval=300000
```

## 8. 异常处理

定义专用异常类型:

```java
public class SnoPoolException extends RuntimeException {
    private final SnoPoolErrorCode errorCode;
    
    public SnoPoolException(SnoPoolErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SnoPoolErrorCode getErrorCode() {
        return errorCode;
    }
}

public enum SnoPoolErrorCode {
    POOL_EXHAUSTED(1001, "学号池已耗尽"),
    SNO_NOT_FOUND(1002, "学号不存在"),
    PERSISTENCE_ERROR(1003, "持久化错误"),
    INVALID_STRATEGY(1004, "无效的超时策略");
    
    private final int code;
    private final String defaultMessage;
    
    SnoPoolErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
```

## 9. 单元测试规范

为确保代码质量，应至少包含以下测试用例：

1. 基本功能测试：分配和回收学号
2. 超时策略测试：不同策略的超时行为
3. 持久化测试：存储和恢复状态
4. 并发测试：多线程环境下的行为
5. 异常处理测试：各种异常情况的处理

## 10. 性能考量

1. 使用并发数据结构保证线程安全
2. 异步保存持久化数据，避免阻塞
3. 定期清理过期数据，避免内存泄漏
4. 使用读写锁分离读写操作，提高并发性能 