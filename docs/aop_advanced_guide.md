# AOP日志系统进阶指南

## 一、功能进阶路线图

### 1. 基础优化（入门级）
#### 1.1 执行时间统计优化
```java
@Around("logPointcut()")
public Object logAround(ProceedingJoinPoint point) throws Throwable {
    // 使用更精确的计时方式
    long startNano = System.nanoTime();
    try {
        return point.proceed();
    } finally {
        long endNano = System.nanoTime();
        // 转换为毫秒，保留3位小数
        double elapsedMs = (endNano - startNano) / 1_000_000.0;
        logger.info("方法执行耗时: {}ms", String.format("%.3f", elapsedMs));
    }
}
```

#### 1.2 异常信息完善
```java
try {
    return point.proceed();
} catch (Exception e) {
    // 记录详细的异常信息
    logger.error("方法执行异常: {}", e.getMessage());
    logger.error("异常堆栈: ", e);
    logger.error("异常发生位置: {}#{}", 
        point.getTarget().getClass().getName(),
        point.getSignature().getName());
    throw e;
}
```

#### 1.3 日志级别调整
```java
// 不同场景使用不同日志级别
logger.debug("开始执行方法: {}", methodName);  // 调试信息
logger.info("方法正常返回: {}", result);       // 正常信息
logger.warn("方法执行时间过长: {}ms", time);   // 警告信息
logger.error("方法执行异常: {}", e);          // 错误信息
```

### 2. 日志持久化（中级）
#### 2.1 日志表设计
```sql
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module VARCHAR(50) COMMENT '模块名称',
    operation_type VARCHAR(20) COMMENT '操作类型',
    method_name VARCHAR(100) COMMENT '方法名',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    error_msg TEXT COMMENT '错误信息',
    execution_time DECIMAL(10,3) COMMENT '执行时间(ms)',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人名称',
    operation_time DATETIME COMMENT '操作时间',
    status TINYINT COMMENT '状态(0:失败,1:成功)',
    INDEX idx_operation_time (operation_time),
    INDEX idx_module (module)
) COMMENT '系统操作日志表';
```

#### 2.2 异步处理实现
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("LogAsync-");
        executor.initialize();
        return executor;
    }
}

@Service
public class LogService {
    @Async
    public void saveLog(OperationLog log) {
        // 异步保存日志
        logMapper.insert(log);
    }
}
```

### 3. 存储策略优化（高级）
#### 3.1 分表策略
```java
public class LogTableStrategy {
    // 按月分表
    public String getTableName() {
        return "sys_operation_log_" + 
               DateUtil.format(new Date(), "yyyyMM");
    }
    
    // 自动创建新表
    public void createTableIfNotExists(String tableName) {
        // 检查表是否存在
        // 不存在则创建新表
    }
}
```

#### 3.2 日志清理机制
```java
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
public void cleanOldLogs() {
    // 保留近3个月的日志
    Date threshold = DateUtil.addMonths(new Date(), -3);
    // 清理旧数据
    logMapper.deleteByTime(threshold);
    // 归档处理
    archiveOldLogs(threshold);
}
```

### 4. 高级特性（专家级）
#### 4.1 ELK集成
```yaml
# logback-spring.xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

#### 4.2 性能优化
```java
public class LogBuffer {
    private static final int BUFFER_SIZE = 1000;
    private static final BlockingQueue<OperationLog> logQueue = 
        new LinkedBlockingQueue<>(BUFFER_SIZE);
    
    // 批量写入
    @Scheduled(fixedRate = 5000)  // 每5秒执行一次
    public void batchSave() {
        List<OperationLog> logs = new ArrayList<>();
        logQueue.drainTo(logs, BUFFER_SIZE);
        if (!logs.isEmpty()) {
            logMapper.batchInsert(logs);
        }
    }
}
```

#### 4.3 监控分析
```java
@Service
public class LogAnalysisService {
    // 接口调用频率统计
    public Map<String, Integer> analyzeApiCalls(Date start, Date end) {
        return logMapper.countByMethod(start, end);
    }
    
    // 性能分析
    public List<MethodPerformance> analyzePerformance() {
        return logMapper.findSlowMethods(1000); // 查找执行时间>1秒的方法
    }
    
    // 异常统计
    public Map<String, Integer> analyzeExceptions(Date start, Date end) {
        return logMapper.countByErrorType(start, end);
    }
}
```

## 二、注意事项

### 1. 性能考虑
- 使用异步处理避免影响主业务流程
- 合理设置日志级别，避免过多DEBUG日志
- 定期清理和归档旧日志
- 使用批量写入优化数据库操作

### 2. 安全考虑
- 敏感信息脱敏处理
- 控制日志访问权限
- 防止日志信息泄露
- 考虑数据合规性要求

### 3. 可维护性
- 日志格式统一规范
- 关键信息完整记录
- 提供日志查询和分析接口
- 制定日志管理制度

## 三、扩展建议

### 1. 监控告警
- 接口调用异常监控
- 性能监控和告警
- 系统错误实时通知

### 2. 统计分析
- 接口调用趋势分析
- 用户行为分析
- 系统性能分析
- 错误分布分析

### 3. 可视化展示
- 操作日志查询界面
- 统计图表展示
- 实时监控大屏
- 性能分析报表

## 四、实施建议

### 1. 循序渐进
1. 先完善基础功能
2. 再实现异步处理
3. 然后优化存储策略
4. 最后添加高级特性

### 2. 重点关注
- 性能影响
- 数据安全
- 存储容量
- 查询效率

### 3. 验证测试
- 单元测试覆盖
- 性能压力测试
- 异常恢复测试
- 功能验证测试 