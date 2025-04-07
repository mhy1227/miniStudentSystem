# 开发指南

## 1. 环境准备
### 1.1 必需环境
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- IDE（推荐IDEA）
- Git

### 1.2 环境配置
1. **JDK配置**
   - 安装JDK 1.8
   - 配置JAVA_HOME环境变量
   - 验证：`java -version`

2. **Maven配置**
   - 安装Maven 3.6+
   - 配置MAVEN_HOME环境变量
   - 配置阿里云镜像源（可选）
   - 验证：`mvn -v`

3. **MySQL配置**
   - 安装MySQL 5.7+
   - 创建数据库：student_db
   - 执行初始化脚本：`src/main/resources/db/init.sql`

## 2. 项目启动
1. **获取代码**
   ```bash
   git clone <项目地址>
   cd student-management
   ```

2. **配置修改**
   - 修改数据库配置：`src/main/resources/jdbc.properties`
   - 修改日志配置（如需要）

3. **编译运行**
   ```bash
   mvn clean package
   mvn tomcat7:run
   ```

4. **访问测试**
   - 访问地址：`http://localhost:8080`
   - 测试接口：`http://localhost:8080/api/students`

## 3. 开发流程
1. **创建功能分支**
   ```bash
   git checkout -b feature/xxx
   ```

2. **开发步骤**
   - 编写实体类
   - 创建Mapper接口
   - 实现Service层
   - 编写Controller
   - 编写单元测试

3. **提交代码**
   ```bash
   git add .
   git commit -m "feat: xxx功能实现"
   git push origin feature/xxx
   ```

## 4. 开发规范
### 4.1 代码结构
- controller层：处理请求响应
- service层：处理业务逻辑
- dao层：数据库操作
- model：实体类定义
- util：工具类

### 4.2 命名规范
- 参考项目规范文档（project-spec.md）

### 4.3 接口规范
- 参考API文档（api-doc.md）

## 5. 常见问题
### 5.1 环境相关
1. **Maven依赖下载失败**
   - 检查网络连接
   - 使用阿里云镜像源
   - 清理本地仓库后重试

2. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证数据库用户名密码
   - 确认数据库是否创建

### 5.2 开发相关
1. **接口404**
   - 检查请求路径是否正确
   - 确认Controller注解是否正确
   - 验证SpringMVC配置

2. **数据库操作异常**
   - 检查SQL语法
   - 确认数据库连接配置
   - 验证实体类映射

## 6. 部署说明
1. **打包**
   ```bash
   mvn clean package -Dmaven.test.skip=true
   ```

2. **部署**
   - 将war包部署到Tomcat的webapps目录
   - 启动Tomcat服务器
   - 访问测试

## 7. 参考资料
- Spring MVC官方文档
- MyBatis官方文档
- Maven官方文档
- Git基础教程 