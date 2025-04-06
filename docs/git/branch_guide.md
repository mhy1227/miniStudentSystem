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

## 二、本次异地登录检测功能分支

### 1. 分支信息
- 分支名：`feature/session-detection`
- 基础分支：`develop`
- 功能描述：实现基于Map的异地登录检测功能

### 2. 创建步骤
```bash
# 1. 确保当前在develop分支且代码是最新的
git checkout develop
git pull origin develop

# 2. 创建新的功能分支
git checkout -b feature/session-detection

# 3. 推送分支到远程仓库
git push --set-upstream origin feature/session-detection
```

### 3. 开发流程
1. 在功能分支上进行开发
2. 定期提交代码并推送到远程
3. 完成功能后合并回develop分支

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
# 1. 切换到develop分支
git checkout develop

# 2. 更新develop分支
git pull origin develop

# 3. 合并功能分支
git merge --no-ff feature/session-detection

# 4. 推送到远程
git push origin develop

# 5. 删除功能分支（可选）
git branch -d feature/session-detection
git push origin --delete feature/session-detection
```

## 三、注意事项

1. **代码提交**
   - 保持提交粒度适中，每个提交都应该是完整的功能点
   - 提交信息要清晰明了，描述清楚改动内容
   - 避免提交与功能无关的文件

2. **分支管理**
   - 及时同步develop分支的更新
   - 解决冲突时要仔细核对代码
   - 功能完成后及时合并和清理分支

3. **代码审查**
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
``` 