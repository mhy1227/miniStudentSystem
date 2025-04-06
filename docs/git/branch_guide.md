# Git分支管理指南

## 一、分支命名规范

### 1. 主要分支
- `main`/`master`: 主分支，用于生产环境
- `develop`: 开发分支，用于开发环境

### 2. 功能分支
- 格式：`feature/功能名称`
- 示例：`feature/session-detection`

### 3. 修复分支
- 格式：`hotfix/问题简述`
- 示例：`hotfix/login-error`

### 4. 发布分支
- 格式：`release/版本号`
- 示例：`release/v1.0.0`

## 二、分支操作流程

### 1. 新功能开发流程

#### 1.1 前置检查
1. 确认当前功能是否依赖其他功能分支
2. 如果有依赖，需要等待依赖的功能分支合并到main后再创建新分支
3. 如果没有依赖，可以直接从main分支创建新分支

#### 1.2 合并依赖分支
如果当前在功能分支A上开发完成，需要开发新功能B，且B依赖于A：
```bash
# 1. 在A分支提交所有修改
git add .
git commit -m "docs: 相关文档更新"

# 2. 切换到main分支并更新
git checkout main
git pull origin main

# 3. 合并A分支到main
git merge feature-A
git push origin main

# 4. 基于更新后的main创建B分支
git checkout -b feature-B
git push --set-upstream origin feature-B
```

#### 1.3 创建新功能分支
```bash
# 1. 确保在main分支
git checkout main
git pull origin main

# 2. 创建并切换到新分支
git checkout -b feature/new-feature

# 3. 推送新分支到远程仓库
git push --set-upstream origin feature/new-feature
```

### 2. 功能开发示例
以当前项目为例，开发异地登录检测功能的完整流程：

```bash
# 1. 当前在feature-login分支，提交修改
git add .
git commit -m "docs: 添加Git分支管理文档和异地登录检测方案"

# 2. 合并登录功能到main分支
git checkout main
git pull origin main
git merge feature-login
git push origin main

# 3. 创建异地登录检测功能分支
git checkout -b feature/session-detection
git push --set-upstream origin feature/session-detection
```

### 3. 开发流程
1. 在功能分支上进行开发
2. 定期提交代码并推送到远程
3. 完成功能后合并回main分支

### 4. 提交规范
提交信息格式：
```
<type>: <subject>

[optional body]
```

type类型：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

示例：
```bash
git commit -m "feat: 添加SessionManager实现异地登录检测"
git commit -m "docs: 更新异地登录检测文档"
```

### 5. 合并流程
```bash
# 1. 切换到main分支
git checkout main

# 2. 更新main分支
git pull origin main

# 3. 合并功能分支
git merge feature/session-detection

# 4. 推送到远程
git push origin main

# 5. 删除功能分支（可选）
git branch -d feature/session-detection
git push origin --delete feature/session-detection
```

## 三、注意事项

1. **分支依赖处理**
   - 在创建新分支前，确认功能依赖关系
   - 依赖的功能分支必须先合并到main
   - 避免分支之间直接合并

2. **代码提交**
   - 保持提交粒度适中，每个提交都应该是完整的功能点
   - 提交信息要清晰明了，描述清楚改动内容
   - 避免提交与功能无关的文件

3. **分支管理**
   - 及时同步main分支的更新
   - 解决冲突时要仔细核对代码
   - 功能完成后及时合并和清理分支

4. **代码审查**
   - 合并前进行自我代码审查
   - 确保代码符合项目规范
   - 测试功能是否正常

## 四、回滚策略

如果需要回滚代码，可以使用以下命令：

```bash
# 查看提交历史
git log

# 回滚到指定提交
git reset --hard <commit-id>

# 强制推送（谨慎使用）
git push -f origin feature/session-detection
```

## 五、常用命令参考

```bash
# 查看所有分支
git branch -a

# 查看当前状态
git status

# 添加所有改动
git add .

# 提交代码
git commit -m "message"

# 推送到远程
git push

# 拉取更新
git pull

# 解决冲突后继续合并
git merge --continue

# 暂存当前修改
git stash

# 恢复暂存的修改
git stash pop

# 查看暂存列表
git stash list

# 删除分支
git branch -d <branch-name>

# 强制删除分支
git branch -D <branch-name>
``` 