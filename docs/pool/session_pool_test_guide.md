# Session池化功能测试指南

## 一、测试环境准备

### 1. 基础环境
- JDK 8+
- Maven 3.6+
- Spring Boot 2.x
- 浏览器（Chrome/Firefox）

### 2. 测试工具
- JUnit 5
- Apache JMeter（性能测试）
- Postman（接口测试）
- Chrome开发者工具

### 3. 测试账号
- 准备至少3个测试用户账号
- 记录每个账号的学号和密码
- 确保账号状态正常（未被锁定）

## 二、功能测试

### 1. 会话池基础功能测试
```java
@Test
void testSessionPoolBasic() {
    // 1. 初始化会话池
    SessionPool pool = new SessionPool(10, 5);
    
    // 2. 获取会话
    UserSession session = pool.borrowSession("test001");
    assertNotNull(session);
    assertEquals("test001", session.getSno());
    
    // 3. 验证会话状态
    assertTrue(session.isInPool());
    assertTrue(pool.isValidSession("test001", session.getSessionId()));
    
    // 4. 归还会话
    pool.returnSession(session);
}
```

### 2. 异地登录检测测试
```java
@Test
void testConcurrentLogin() {
    SessionPool pool = new SessionPool(10, 5);
    
    // 1. 第一次登录
    UserSession session1 = pool.borrowSession("test001");
    session1.setIp("192.168.1.1");
    
    // 2. 尝试异地登录
    assertThrows(SessionException.class, () -> {
        pool.borrowSession("test001");
    });
    
    // 3. 验证当前登录IP
    assertEquals("192.168.1.1", pool.getCurrentLoginIp("test001"));
}
```

### 3. 会话超时测试
```java
@Test
void testSessionTimeout() {
    // 使用较短的超时时间进行测试
    SessionPool pool = new SessionPool(10, 5, 2, 1000, 1000);
    
    // 1. 获取会话
    UserSession session = pool.borrowSession("test001");
    
    // 2. 等待超时
    Thread.sleep(2000);
    
    // 3. 验证会话已失效
    assertFalse(pool.isValidSession("test001", session.getSessionId()));
}
```

## 三、集成测试

### 1. 登录流程测试
1. 访问登录页面：`http://localhost:8080/login.html`
2. 输入测试账号信息
3. 验证登录成功
4. 检查会话状态

### 2. 异地登录测试
1. 浏览器A登录账号
2. 浏览器B（无痕模式）尝试登录同一账号
3. 验证提示"该账号已在其他地方登录"
4. 检查A的会话状态保持不变

### 3. 会话管理测试
1. 正常登录后执行以下操作：
   - 刷新页面，验证会话保持
   - 等待5分钟，验证会话活跃
   - 模拟网络断开重连，验证会话恢复
   - 执行登出，验证会话清理

## 四、性能测试（使用JMeter）

### 1. 并发登录测试
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Session Pool Test">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <stringProp name="TestPlan.comments"></stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Concurrent Login Test">
        <intProp name="ThreadGroup.num_threads">100</intProp>
        <intProp name="ThreadGroup.ramp_time">10</intProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">false</boolProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 2. 会话池容量测试
1. 设置较小的池容量（如maxTotal=20）
2. 使用JMeter模拟30个并发用户
3. 观察以下指标：
   - 会话获取成功率
   - 响应时间变化
   - 系统资源使用情况

### 3. 长期稳定性测试
1. 持续运行系统24小时
2. 定期执行登录/登出操作
3. 监控关键指标：
   - 内存使用
   - 会话数量
   - GC情况
   - 响应时间

## 五、监控指标

### 1. 基础指标
- 活跃会话数
- 空闲会话数
- 创建会话总数
- 丢弃会话数

### 2. 性能指标
- 会话获取平均时间
- 会话池使用率
- GC频率和时间
- 内存使用情况

### 3. 异常指标
- 获取会话失败次数
- 会话验证失败次数
- 异地登录尝试次数
- 超时会话数量

## 六、问题排查指南

### 1. 常见问题
1. 会话获取超时
   - 检查池容量配置
   - 查看当前活跃会话数
   - 分析会话获取耗时

2. 内存使用过高
   - 检查会话对象大小
   - 分析GC日志
   - 调整池容量参数

3. 异地登录检测失效
   - 验证snoToSessionId映射
   - 检查会话状态更新
   - 查看日志记录

### 2. 日志分析
```shell
# 查看错误日志
grep "ERROR" server.log | grep "SessionPool"

# 分析会话操作
grep "Session" server.log | grep -E "borrow|return|invalidate"

# 统计异地登录次数
grep "该账号已在其他地方登录" server.log | wc -l
```

## 七、测试用例清单

### 1. 单元测试
- [ ] SessionPool基础功能测试
- [ ] 会话生命周期测试
- [ ] 并发控制测试
- [ ] 异常处理测试

### 2. 集成测试
- [ ] 登录流程测试
- [ ] 会话验证测试
- [ ] 异地登录测试
- [ ] 超时处理测试

### 3. 性能测试
- [ ] 并发登录测试
- [ ] 容量上限测试
- [ ] 长期稳定性测试
- [ ] 内存泄漏测试

## 八、测试结果记录模板

### 功能测试记录
| 测试项 | 测试步骤 | 预期结果 | 实际结果 | 是否通过 | 备注 |
|-------|---------|----------|----------|----------|------|
| 登录   | 1. 输入账号密码<br>2. 点击登录 | 登录成功 | | | |
| 异地登录 | 1. A浏览器登录<br>2. B浏览器登录 | B登录失败 | | | |
| 会话超时 | 等待30分钟 | 自动登出 | | | |

### 性能测试记录
| 测试场景 | 并发用户数 | 平均响应时间 | 成功率 | 内存使用 | TPS |
|---------|-----------|-------------|--------|----------|-----|
| 并发登录 | 100 | | | | |
| 持续运行 | 50 | | | | |
| 容量测试 | 200 | | | | | 