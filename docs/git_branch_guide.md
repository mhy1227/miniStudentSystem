# Git分支管理指南

## 分支命名规范
1. 功能开发分支：`feature/功能名称`
   - 例如：`feature/aop-logging`
   - 例如：`feature/user-auth`

2. 问题修复分支：`bugfix/问题描述`
   - 例如：`bugfix/student-list-404`
   - 例如：`bugfix/course-save-error`

3. 紧急修复分支：`hotfix/问题描述`
   - 例如：`hotfix/security-vulnerability`
   - 例如：`hotfix/database-connection`

## 开发流程
### 方式一：命令行操作
1. 创建新功能分支
```bash
# 确保当前在main分支且代码是最新的
git checkout main
git pull

# 创建并切换到新分支
git checkout -b feature/aop-logging
```

2. 开发过程中的提交
```bash
# 查看变更文件
git status

# 添加变更文件
git add 文件路径

# 提交变更
git commit -m "类型: 提交描述

- 具体变更点1
- 具体变更点2"
```

3. 合并到主分支
```bash
# 切换到main分支
git checkout main

# 拉取最新代码
git pull

# 合并功能分支
git merge feature/aop-logging

# 推送到远程
git push
```

### 方式二：GitLens图形化操作
1. 创建新分支
   - 点击左下角的分支名称
   - 在弹出的命令面板中选择"Create Branch..."
   - 输入新分支名称（如：`feature/aop-logging`）
   - 选择基于当前分支（main）创建

2. 提交更改
   - 打开源代码管理视图（Ctrl+Shift+G）
   - 查看更改的文件列表
   - 点击文件旁边的 "+" 暂存更改
   - 在消息框中输入提交信息
   - 点击"提交"按钮（✓）或按 Ctrl+Enter 完成提交

3. 查看历史
   - 文件历史：右键文件 -> GitLens -> Open File History
   - 目录历史：右键目录 -> GitLens -> Open Folder History
   - 分支图：点击左侧活动栏的 Git Graph 图标

4. 合并分支
   - 切换到目标分支（点击左下角分支名）
   - 在源代码管理视图中右键点击要合并的分支
   - 选择"Merge Branch into Current Branch"
   - 点击同步按钮推送到远程

## 提交信息规范
1. 类型前缀：
   - `feat`: 新功能
   - `fix`: 修复问题
   - `docs`: 文档更新
   - `style`: 代码格式调整
   - `refactor`: 代码重构
   - `test`: 测试相关
   - `chore`: 其他修改

2. 格式示例：
```bash
feat: 添加AOP日志功能

- 新增Log注解
- 实现LogAspect切面
- 添加单元测试
```

## AOP日志功能开发示例
### 命令行方式
1. 创建功能分支
```bash
git checkout -b feature/aop-logging
```

2. 提交基础设施代码
```bash
git add src/main/java/com/czj/student/aspect/LogAspect.java
git add src/main/java/com/czj/student/annotation/Log.java
git commit -m "feat: 添加AOP日志基础设施

- 新增Log注解用于标记需要记录日志的方法
- 新增LogAspect切面类实现日志记录
- 支持方法执行时间、参数、返回值等信息记录"
```

3. 提交Controller注解应用
```bash
git add src/main/java/com/czj/student/controller/StudentController.java
git commit -m "feat: 在StudentController中应用Log注解

- 为CRUD接口添加Log注解
- 添加模块名称、操作类型和描述信息
- 用于测试AOP日志功能"
```

4. 合并到main
```bash
git checkout main
git merge feature/aop-logging
git push
```

### GitLens方式
1. 创建分支
   - 点击左下角分支名称
   - 选择"Create Branch..."
   - 输入`feature/aop-logging`
   - 选择基于main创建

2. 提交代码
   - 在源代码管理视图中：
     - 选择要提交的文件（LogAspect.java和Log.java）
     - 点击 "+" 暂存更改
     - 输入提交信息
     - 点击"提交"按钮

3. 合并到main
   - 切换到main分支
   - 右键点击feature/aop-logging分支
   - 选择"Merge Branch into Current Branch"
   - 点击同步按钮推送到远程

## 注意事项
1. 每个功能都创建独立的分支
2. 保持提交信息清晰和规范
3. 合并前确保代码经过测试
4. 定期同步main分支的更新
5. 合并后删除不再使用的功能分支
6. 使用GitLens的文件历史功能追踪代码变更
7. 定期使用"Git Graph"检查分支状态 