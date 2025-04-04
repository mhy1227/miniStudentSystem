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

## 常见问题
### 1. 换行符警告
当看到类似这样的警告时：
```bash
warning: in the working copy of '...', LF will be replaced by CRLF the next time Git touches it
```

这是由于Windows和Linux/Mac系统使用不同的换行符导致的：
- Windows: CRLF（\r\n）
- Linux/Mac: LF（\n）

解决方案：
```bash
# 方案一：忽略警告（不影响代码运行）
# 继续开发工作即可

# 方案二：关闭自动转换
git config --global core.autocrlf false

# 方案三：启用自动转换（推荐Windows用户使用）
git config --global core.autocrlf true
```

建议Windows用户使用方案三，这样可以：
- 提交时自动将CRLF转换为LF
- 检出时自动将LF转换为CRLF
- 保持跨平台协作的一致性 

### 2. 提交失败问题
当看到类似这样的提示时：
```bash
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git restore <file>..." to discard changes in working directory)
        modified:   some_file.txt

no changes added to commit (use "git add" and/or "git commit -a")
```

这表示要提交的文件还没有被暂存（没有执行git add）。

### 3. 推送新分支失败
当看到类似这样的提示时：
```bash
fatal: The current branch feature/aop-logging has no upstream branch.
To push the current branch and set the remote as upstream, use

    git push --set-upstream origin feature/aop-logging
```

这是因为新创建的分支只在本地存在，还没有与远程仓库建立关联。可以理解为：
- 本地分支：在你的电脑上已经创建好了
- 远程分支：在GitHub/GitLab上还不存在
- 需要建立关联：告诉Git本地分支对应远程的哪个分支

解决方案：

```bash
# 方案一：手动建立关联（推荐新手使用）
git push --set-upstream origin feature/aop-logging
# 或使用简写形式
git push -u origin feature/aop-logging    # 注意：必须包含 origin

# 错误写法示例：
git push -u feature/aop-logging    # ❌ 错误：缺少 origin
git push --set-upstream feature/aop-logging    # ❌ 错误：缺少 origin

# 方案二：设置Git自动建立关联（推荐老手使用）
git config --global push.autoSetupRemote true
git push  # 之后直接push就可以了
```

命令格式说明：
- `origin`：远程仓库的默认名称
- `feature/aop-logging`：要推送的分支名称
- `-u` 或 `--set-upstream`：建立本地和远程分支的关联

两种方案的区别：
1. 手动建立关联
   - 每个新分支第一次推送时都需要使用 -u 参数
   - 更清晰地知道发生了什么
   - 适合初学者使用，加深理解

2. 自动建立关联
   - 一次性设置，永久生效
   - 以后创建新分支直接 git push 即可
   - 适合熟手使用，提高效率

设置了自动建立关联后，后续只需要执行 `git push` 即可。

## 最佳实践
### 1. 提交前检查
```bash
# 查看当前状态
git status

# 查看具体改动
git diff
```

### 2. 精确的暂存操作
```bash
# 不推荐：直接添加所有改动
git add .

# 推荐：明确指定要提交的文件
git add src/main/java/com/czj/student/controller/StudentController.java
```

### 3. 相关性提交原则
1. 不同类型的修改应该分开提交：
   ```bash
   # 先提交代码修改
   git add src/main/java/com/czj/student/controller/StudentController.java
   git commit -m "feat: 在StudentController中应用Log注解"

   # 再提交文档更新
   git add docs/git_branch_guide.md
   git commit -m "docs: 更新Git分支管理指南"
   ```

2. 提交信息要明确指出改动内容：
   ```bash
   # 不好的示例
   git commit -m "更新代码"

   # 好的示例
   git commit -m "feat: 在StudentController中应用Log注解

   - 为CRUD接口添加Log注解
   - 添加模块名称、操作类型和描述信息
   - 用于测试AOP日志功能"
   ```

### 4. 提交前复查清单
1. 检查工作区状态：`git status`
2. 检查具体改动：`git diff`
3. 确认改动相关性：相关的改动放在一起提交
4. 编写清晰的提交信息：
   - 使用正确的类型前缀
   - 简要说明改动内容
   - 列出具体的改动点

### 5. 撤销操作
如果发现提交有误，可以使用以下命令：
```bash
# 撤销暂存的文件
git reset HEAD <file>

# 撤销最后一次提交（保留修改）
git reset --soft HEAD^

# 撤销最后一次提交（丢弃修改）
git reset --hard HEAD^

# 修改最后一次提交信息
git commit --amend
```

注意：如果已经推送到远程，请谨慎使用reset命令。

### 6. 分支切换操作
#### 基础命令
```bash
# 查看所有分支（*号表示当前分支）
git branch

# 切换到main分支
git checkout main
# 或使用新版命令
git switch main

# 切换回功能分支
git checkout feature/aop-logging
# 或
git switch feature/aop-logging
```

#### 切换注意事项
1. 切换前检查工作区状态
```bash
# 检查是否有未提交的修改
git status
```

2. 处理未提交的修改
```bash
# 方案1：提交修改
git add .
git commit -m "你的提交信息"
git push

# 方案2：暂存修改
git stash  # 暂存当前修改
git checkout main  # 切换分支
git stash pop  # 恢复暂存的修改
```

3. 完整的分支切换流程
```bash
# 1. 检查当前状态
git status

# 2. 如果有修改，先提交
git add .
git commit -m "你的提交信息"
git push

# 3. 切换到main分支
git checkout main

# 4. 拉取main分支最新代码
git pull
```

4. 常见问题
- 如果有未提交的修改，Git会阻止分支切换
- 如果修改的文件在目标分支也有修改，可能会产生冲突
- 建议养成经常提交的习惯，避免积累大量修改 