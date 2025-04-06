# 登录功能Git操作指南

## 一、分支创建与管理
1. **创建并切换到登录功能分支**
```bash
# 确保当前在main分支并更新到最新
git checkout main
git pull

# 创建并切换到feature-login分支
git checkout -b feature-login
```

## 二、提交规范
### 1. 提交信息格式
```
<type>: <subject>

[body]
```

### 2. type类型
- feat: 新功能
- fix: 修复bug
- docs: 文档修改
- style: 代码格式修改
- refactor: 代码重构
- test: 测试用例修改
- chore: 其他修改

### 3. 示例
```bash
# 文档提交
git add docs/login_guide.md docs/login_progress.md docs/login_git_guide.md
git commit -m "docs: 添加登录功能相关文档
- 添加登录功能设计方案
- 添加开发进度记录
- 添加git操作指南"

# 数据库脚本提交
git add src/main/resources/db/update/V1__add_login_fields.sql src/main/resources/db/update/V1__update_login_data.sql
git commit -m "feat: 添加登录功能数据库脚本
- 添加pwd和login_error_count字段
- 添加初始密码设置脚本"

# 后端代码提交
git add src/main/java/com/czj/student/controller/LoginController.java
git add src/main/java/com/czj/student/service/LoginService.java
git commit -m "feat: 添加登录功能后端代码
- 实现登录控制器
- 实现登录服务
- 添加登录拦截器"

# 前端代码提交
git add src/main/webapp/login.html src/main/webapp/js/login.js
git commit -m "feat: 添加登录功能前端代码
- 添加登录页面
- 实现登录交互逻辑
- 添加错误提示"
```

## 三、开发流程
1. **功能开发**
   - 每个相对独立的功能点完成后进行提交
   - 提交信息要清晰表达改动内容
   - 相关文件一起提交

2. **代码同步**
   ```bash
   # 定期同步主分支的更新
   git checkout main
   git pull
   git checkout feature-login
   git merge main
   ```

3. **功能完成**
   ```bash
   # 功能开发完成后合并到主分支
   git checkout main
   git merge feature-login
   git push
   ```

## 四、注意事项
1. 每次提交前先pull最新代码
2. 提交信息要清晰明了
3. 相关联的改动最好一起提交
4. 定期同步主分支的更新
5. 解决冲突时要谨慎

## 五、开发计划提交节点
1. **初始化阶段**
   - 文档提交（设计方案、进度记录、git指南）
   - 数据库脚本提交

2. **基础功能阶段**
   - 登录基础类提交（VO类、常量类）
   - 登录服务提交
   - 登录控制器提交
   - 登录拦截器提交

3. **前端开发阶段**
   - 登录页面提交
   - 登录交互提交
   - 系统集成提交

4. **测试完善阶段**
   - 测试用例提交
   - Bug修复提交
   - 文档更新提交

## 六、Git推送说明
### 1. 推送命令格式
```bash
# 完整格式
git push <remote> <branch>

# 设置上游分支并推送
git push -u <remote> <branch>

# 简化推送（需要先设置过上游分支）
git push
```

### 2. 分支关联说明
1. **首次推送新分支**
   ```bash
   # 第一次推送feature-login分支
   git push -u origin feature-login
   ```
   - `-u` 或 `--set-upstream` 用于设置上游分支
   - 设置后，本地分支会与远程分支建立关联
   - 这个操作只需要做一次

2. **切换分支后推送**
   - 如果是首次推送该分支，需要设置关联：
     ```bash
     git checkout feature-login
     git push -u origin feature-login
     ```
   - 如果之前已经设置过关联：
     ```bash
     git checkout feature-login
     git push
     ```

3. **查看分支关联状态**
   ```bash
   # 查看本地分支与远程分支的关联
   git branch -vv
   
   # 查看所有分支信息
   git branch -a
   ```

### 3. 最佳实践
1. **创建新分支时**
   ```bash
   # 从main分支创建并切换到新分支
   git checkout -b feature-login
   
   # 首次推送时设置关联
   git push -u origin feature-login
   ```

2. **日常开发推送**
   ```bash
   # 添加修改
   git add .
   
   # 提交修改
   git commit -m "fix: 修复xxx问题"
   
   # 推送到远程
   git push
   ```

3. **多分支协作时**
   - 切换分支前先提交或暂存当前修改
   - 切换到新分支后第一次推送要设置关联
   - 使用 `git status` 检查分支状态
   - 使用 `git branch -vv` 检查分支关联

### 4. 注意事项
1. 推送前先拉取最新代码：
   ```bash
   git pull
   ```

2. 解决冲突后再推送：
   ```bash
   git pull
   # 解决冲突
   git add .
   git commit -m "fix: 解决冲突"
   git push
   ```


3. 分支命名规范：
   - 功能分支：feature/xxx
   - 修复分支：fix/xxx
   - 版本分支：release/xxx

4. 错误处理：
   - 如果推送失败，检查远程仓库地址
   - 检查是否有权限推送
   - 检查分支名称是否正确 

## 异地登录功能分支合并
```
# 1. 确保当前分支的所有更改都已提交
git add .
git commit -m "feat: 完成异地登录检测功能

1. 核心功能实现
- 添加SessionManager实现会话管理
- 添加IpUtil工具类处理IP地址
- 添加SessionConfig支持定时任务

2. 系统集成
- 改造LoginController支持异地登录检测
- 增强LoginInterceptor添加会话验证
- 优化登录页面错误提示

3. 文档完善
- 添加异地登录检测方案文档
- 更新实现进度文档
- 添加测试指南文档

4. 功能特性
- 支持异地登录检测
- 自动清理过期会话
- 友好的错误提示
- 完整的日志记录"

# 2. 切换到main分支并更新
git checkout main
git pull origin main

# 3. 合并功能分支
git merge feature/session-detection

# 4. 推送到远程仓库
git push origin main

# 5. （可选）删除功能分支
git branch -d feature/session-detection
git push origin --delete feature/session-detection
```