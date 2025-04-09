# SnoPool学号池最大值管理与学号复用策略

## 1. 学号结构与生成策略

### 1.1 学号构成

学号采用"XH"前缀加6位数字的格式：
- 前缀：XH（表示学号）
- 数字部分：000001~999999，固定6位，不足前补0

### 1.2 现有问题分析

当前的SnoPool在学号管理上存在以下问题：

1. 使用Map结构存储，无法直接获取最大学号
2. 初始化时需要从数据库查询当前最大学号
3. 学号回收后的复用策略不够优化
4. 重启时可能丢失内存中的学号分配状态

## 2. 优化方案

### 2.1 双层学号管理结构

我们将设计一个双层结构来管理学号：

```
+-----------------------------------+
|            SnoManager             |  <- 学号生成与管理层
+-----------------------------------+
            |        ^
            v        |
+-----------------------------------+
|          SnoAllocator             |  <- 学号分配与回收层
+-----------------------------------+
```

### 2.2 学号管理层（SnoManager）

负责维护最大学号和空闲学号池：

```java
public class SnoManager {
    // 当前最大学号序列值
    private final AtomicInteger maxSequence;
    
    // 回收的学号队列 - 优先分配
    private final PriorityQueue<Integer> recycledSequences;
    
    // 初始化
    public SnoManager(int initialMaxSequence) {
        this.maxSequence = new AtomicInteger(initialMaxSequence);
        // 使用最小堆实现优先队列，确保回收的学号按照升序复用
        this.recycledSequences = new PriorityQueue<>();
    }
    
    // 获取下一个可用学号序列值
    public synchronized int nextSequence() {
        // 优先使用回收的学号
        if (!recycledSequences.isEmpty()) {
            return recycledSequences.poll();
        }
        // 没有回收的学号时，使用最大序列值+1
        return maxSequence.incrementAndGet();
    }
    
    // 回收学号
    public synchronized void recycleSequence(int sequence) {
        // 只回收比当前最大序列小的值，避免重复
        if (sequence > 0 && sequence <= maxSequence.get()) {
            recycledSequences.offer(sequence);
        }
    }
    
    // 获取当前最大序列值
    public int getCurrentMaxSequence() {
        return maxSequence.get();
    }
    
    // 获取当前回收队列大小
    public int getRecycledCount() {
        return recycledSequences.size();
    }
}
```

### 2.3 学号分配与回收层（SnoAllocator）

负责学号的具体分配和回收操作：

```java
public class SnoAllocator {
    private final SnoManager snoManager;
    private final Map<String, SnoAllocation> allocations;
    private final Map<Integer, String> sequenceToSno;
    
    public SnoAllocator(SnoManager snoManager) {
        this.snoManager = snoManager;
        this.allocations = new ConcurrentHashMap<>();
        this.sequenceToSno = new ConcurrentHashMap<>();
    }
    
    // 分配学号
    public String allocate(String userId, SnoTimeoutStrategy strategy) {
        int sequence = snoManager.nextSequence();
        String sno = formatSno(sequence);
        
        SnoAllocation allocation = new SnoAllocation(
            sno, userId, System.currentTimeMillis(), strategy, false);
        
        allocations.put(sno, allocation);
        sequenceToSno.put(sequence, sno);
        
        return sno;
    }
    
    // 回收学号
    public boolean recycle(String sno) {
        SnoAllocation allocation = allocations.remove(sno);
        if (allocation != null) {
            int sequence = extractSequence(sno);
            sequenceToSno.remove(sequence);
            snoManager.recycleSequence(sequence);
            return true;
        }
        return false;
    }
    
    // 格式化学号
    private String formatSno(int sequence) {
        return "XH" + String.format("%06d", sequence);
    }
    
    // 提取序列值
    private int extractSequence(String sno) {
        if (sno != null && sno.startsWith("XH")) {
            try {
                return Integer.parseInt(sno.substring(2));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
```

## 3. 初始化策略

系统启动时需要正确初始化最大学号值：

```java
@PostConstruct
public void init() {
    int maxSequence = 0;
    
    // 1. 从数据库获取当前最大学号
    try {
        String maxDbSno = studentMapper.getMaxSno();
        if (maxDbSno != null && maxDbSno.startsWith("XH")) {
            int dbSequence = Integer.parseInt(maxDbSno.substring(2));
            maxSequence = Math.max(maxSequence, dbSequence);
        }
    } catch (Exception e) {
        logger.warn("从数据库读取最大学号失败", e);
    }
    
    // 2. 从持久化存储加载已分配记录
    try {
        List<SnoAllocation> storedAllocations = repository.findAll();
        for (SnoAllocation allocation : storedAllocations) {
            String sno = allocation.getSno();
            int sequence = extractSequence(sno);
            maxSequence = Math.max(maxSequence, sequence);
            
            // 恢复分配状态
            allocations.put(sno, allocation);
            sequenceToSno.put(sequence, sno);
        }
    } catch (Exception e) {
        logger.warn("从持久化存储加载学号分配记录失败", e);
    }
    
    // 3. 初始化学号管理器
    this.snoManager = new SnoManager(maxSequence);
    
    logger.info("学号池初始化完成，当前最大学号序列: {}", maxSequence);
}
```

## 4. 学号复用优化策略

### 4.1 复用顺序

学号复用采用以下优先顺序：

1. 优先使用数值较小的回收学号（从小到大）
2. 当没有可用的回收学号时，才使用新的最大序列值+1

这样可以确保：
- 学号尽可能保持在较小的范围内
- 减少学号数值的膨胀速度
- 提高学号的重复利用率

### 4.2 复用策略的优势

这种复用策略可以有效解决以下问题：

1. **资源利用效率**：确保回收的学号得到充分利用
2. **数值控制**：避免学号数值不必要的增长
3. **冲突避免**：确保分配时不会产生重复学号
4. **性能优化**：降低查询最大学号的频率

### 4.3 复用算法复杂度

- 分配学号：O(log n)，其中n为回收学号数量
- 回收学号：O(log n)，插入优先队列的时间复杂度

## 5. 持久化与恢复策略

为确保学号分配状态的可靠性，我们采用以下持久化策略：

1. **定时持久化**：每隔一段时间将当前分配状态持久化保存
2. **即时持久化**：关键操作（如分配永久学号）立即触发持久化
3. **状态恢复**：系统启动时，从持久化存储恢复分配状态

持久化数据包括：

1. 当前最大学号序列值
2. 已分配的学号记录
3. 已回收但未重新分配的学号列表

## 6. 并发控制

优化的学号池支持高并发环境：

1. **SnoManager** 中的关键方法使用synchronized保证原子性
2. **分配映射** 使用ConcurrentHashMap确保线程安全
3. **回收队列** 在同步代码块中操作，避免并发修改异常

## 7. 与现有系统的兼容性

新设计保持与现有SnoPool接口的兼容性：

```java
public class EnhancedSnoPool {
    private final SnoManager snoManager;
    private final SnoAllocator snoAllocator;
    
    // 兼容原有SnoPool的方法
    public String allocateSno(String userId) {
        return snoAllocator.allocate(userId, SnoTimeoutStrategy.REGISTRATION);
    }
    
    public boolean recycleSno(String sno) {
        return snoAllocator.recycle(sno);
    }
    
    // 新增的增强方法
    public String allocateSno(String userId, SnoTimeoutStrategy strategy) {
        return snoAllocator.allocate(userId, strategy);
    }
    
    // 其他方法...
}
``` 