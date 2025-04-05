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