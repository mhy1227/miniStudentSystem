# 登录功能开发Git操作指南

## 分支管理
### 1. 创建功能分支
```bash
# 确保在main分支上且是最新代码
git checkout main
git pull origin main

# 创建并切换到登录功能分支
git checkout -b feature-login
```

### 2. 日常开发
```bash
# 查看工作区状态
git status

# 添加修改的文件
git add .

# 提交修改
git commit -m "feat: 添加登录功能相关文件"
```

### 3. 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式（不影响代码运行的变动）
- refactor: 重构
- test: 增加测试
- chore: 构建过程或辅助工具的变动

### 4. 分支合并
```bash
# 切换到main分支
git checkout main

# 合并登录功能分支
git merge feature-login

# 推送到远程
git push origin main
```

## 开发流程
1. 创建功能分支
2. 添加登录相关文档
   - login_guide.md
   - login_progress.md
   - login_git_guide.md
3. 提交文档变更
4. 开发登录功能
   - 每个功能点完成后及时提交
   - 提交信息要清晰明了
5. 完成功能后合并到main分支

## 常见问题处理
### 1. 代码冲突
```bash
# 先获取最新代码
git fetch origin

# 查看差异
git diff feature-login origin/main

# 解决冲突后
git add .
git commit -m "fix: 解决代码冲突"
```

### 2. 撤销修改
```bash
# 撤销工作区修改
git checkout -- <file>

# 撤销暂存区修改
git reset HEAD <file>

# 撤销提交
git reset --soft HEAD^
```

### 3. 临时保存
```bash
# 保存当前修改
git stash save "临时保存登录功能代码"

# 查看stash列表
git stash list

# 恢复修改
git stash pop
```

## 注意事项
1. 每天开发前先pull最新代码
2. 及时提交，避免积累太多修改
3. 提交前先进行代码review
4. 保持提交信息清晰准确
5. 重要节点及时推送到远程仓库 

git add docs/login_git_guide.md docs/login_guide.md docs/login_progress.md src/main/resources/db/update/V1__add_login_fields.sql
git commit -m "docs: 恢复登录相关文档" 