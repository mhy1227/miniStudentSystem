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
- [ ] 代码实现
  - [ ] 基础框架搭建
  - [ ] SnoInfo内部类定义
  - [ ] 并发数据结构实现
  - [ ] 学号生成与分配算法

### 3. 与SessionPool的关系
- SessionPool：已完成开发和部署的会话管理池
- SnoPool：计划开发的学号管理池
- 两者是**完全独立**的组件，解决不同的业务问题：
  - SessionPool处理用户会话管理和异地登录检测
  - SnoPool将处理学号生成、分配和回收

## 二、规划的功能模块

### 1. 核心功能
- [ ] SnoPool类设计与实现
  - [ ] 基础框架搭建
  - [ ] SnoInfo内部类定义
  - [ ] 并发数据结构实现
  - [ ] 学号生成与分配算法

### 2. 学号管理
- [ ] 学号分配
  - [ ] 优先使用回收学号
  - [ ] 自动生成新学号
  - [ ] 学号唯一性保证
  - [ ] 并发分配处理

- [ ] 学号回收
  - [ ] 学号状态标记
  - [ ] 回收队列管理
  - [ ] 资源清理

### 3. 持久化设计
- [ ] 数据库持久化
  - [ ] 学号信息表设计
  - [ ] 批量操作优化
  - [ ] 事务处理

- [ ] Redis集成
  - [ ] 缓存结构设计
  - [ ] 原子操作支持
  - [ ] 过期策略

## 三、接口设计

### 1. 学号分配接口
- [ ] 基础分配接口
  - [ ] 参数验证
  - [ ] 学号分配策略
  - [ ] 返回结果封装

### 2. 学号查询接口
- [ ] 学号状态查询
  - [ ] 按学号查询
  - [ ] 批量查询支持
  - [ ] 状态过滤

### 3. 管理接口
- [ ] 统计信息接口
  - [ ] 分配情况统计
  - [ ] 使用率计算
  - [ ] 状态分布分析

## 四、安全设计

### 1. 基础安全
- [ ] 并发控制
  - [ ] 锁机制设计
  - [ ] 原子操作
  - [ ] 线程安全保证

- [ ] 异常处理
  - [ ] 边界情况处理
  - [ ] 错误恢复机制
  - [ ] 日志记录

### 2. 学号安全
- [ ] 学号生命周期管理
  - [ ] 分配时间记录
  - [ ] 状态变更追踪
  - [ ] 异常状态处理

## 五、开发计划

### 1. 第一阶段：基础实现（2周）
- [ ] 完成核心类设计
- [ ] 实现基础分配机制
- [ ] 添加学号回收功能
- [ ] 编写单元测试

### 2. 第二阶段：功能完善（2周）
- [ ] 实现持久化机制
- [ ] 添加管理接口
- [ ] 完善异常处理
- [ ] 进行集成测试

### 3. 第三阶段：优化与部署（1周）
- [ ] 性能优化
- [ ] 接口完善
- [ ] 文档更新
- [ ] 生产环境部署

## 六、参考经验

### 1. SessionPool经验借鉴
- 可借鉴SessionPool的池化设计模式
- 参考其并发控制机制
- 学习其异常处理策略
- 借鉴性能优化措施

### 2. 不同之处
- 学号池更注重资源分配而非会话跟踪
- 学号有更强的持久化需求
- 学号生成有特定的格式和规则
- 回收策略有所不同

## 七、风险评估

### 1. 技术风险
- **内存占用**：学号信息可能占用较大内存
- **并发控制**：高并发场景下的性能瓶颈
- **数据一致性**：学号唯一性保证的挑战

### 2. 业务风险
- **学号冲突**：可能与现有学号系统冲突
- **学号耗尽**：在极端情况下可能耗尽可用学号
- **业务适配**：与现有业务流程的集成问题

### 3. 缓解措施
- 前期充分的需求分析和设计评审
- 分阶段实施，先小规模测试
- 制定明确的回滚方案
- 建立完善的监控机制

## 八、决策待定事项

### 1. 技术选型
- 是否需要使用Redis作为缓存层
- 选择何种持久化策略
- 是否需要支持分布式部署

### 2. 实现范围
- 是否包含学号格式定制功能
- 是否支持批量操作
- 是否需要提供管理界面

### 3. 优先级确定
- 确定各功能模块的开发优先级
- 明确最小可行产品(MVP)的范围
- 决定哪些功能可以后期迭代 