# 学生信息管理系统

## 项目简介
一个基于SpringMVC的学生管理系统，目前已实现登录功能、会话管理和学号池等核心组件，主要用于学习和演示目的。

## 项目目标
- 实现基础的学生信息管理功能
- 学习SpringMVC框架的开发流程
- 实践会话管理和资源池化设计
- 理解MVC设计模式和RESTful API设计

## 已实现功能
- **用户登录与会话管理**
  - 基本的登录认证
  - 会话状态维护
  - 异地登录检测功能
  
- **学号池管理系统**
  - 基础的学号生成与分配
  - 学号回收与重用机制
  - 线程安全的并发处理

## 待实现功能
- 学生信息的增删改查
- 课程管理功能
- 选课管理功能
- 成绩管理功能
- 权限管理系统

## 技术组件
- **会话池（Session Pool）**
  - 管理用户会话状态
  - 实现简单的异地登录检测
  - 会话超时自动清理
  
- **学号池（Sno Pool）**
  - 内存存储的学号资源池
  - 学号的分配与回收
  - 并发安全设计

## 技术栈
- **后端**
  - Spring MVC 5.3.x
  - MyBatis 3.5.x
  - MySQL 8.0
  - Druid 连接池
  
- **前端**
  - HTML + CSS
  - JavaScript + jQuery
  - Bootstrap 基础组件
  
- **开发工具**
  - Maven 构建
  - Git 版本控制

## 项目结构
```
miniStudentSystem/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/czj/student/
│   │   │       ├── common/       # 通用工具
│   │   │       ├── controller/   # 控制器
│   │   │       ├── dao/          # 数据访问
│   │   │       ├── interceptor/  # 拦截器
│   │   │       ├── model/        # 数据模型
│   │   │       ├── service/      # 服务层
│   │   │       ├── session/      # 会话管理
│   │   │       └── snopool/      # 学号池
│   │   ├── resources/            # 配置文件
│   │   └── webapp/               # Web资源
│   └── test/                     # 测试代码
└── docs/                         # 文档
    ├── git/                      # Git指南
    ├── login/                    # 登录文档
    ├── pool/                     # 池设计文档
    └── mvc_to_boot/              # 框架迁移指南
```

## 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Tomcat 8.5+

## 如何运行
1. 克隆项目
   ```
   git clone https://github.com/mhy1227/miniStudentSystem.git
   ```

2. 配置数据库
   - 创建名为`student_system`的数据库
   - 导入`sql/init.sql`脚本(如有)
   - 修改`src/main/resources/jdbc.properties`中的数据库配置

3. 编译打包
   ```
   mvn clean package
   ```

4. 部署运行
   - 将war包部署到Tomcat下
   - 或使用Maven插件启动：`mvn tomcat7:run`

5. 访问系统
   - 浏览器访问：`http://localhost:8080/`
   - 默认用户名密码：见开发文档

## 开发指南

### 分支管理
- `main`: 主分支
- `feature/*`: 功能分支
- `hotfix/*`: 修复分支

详见 `docs/git/branch_guide.md`

### 主要模块文档
- 会话管理: `docs/login/session_detection.md`
- 学号池: `docs/pool/student_number_pool_design.md`
- SpringBoot迁移参考: `docs/mvc_to_boot/migration_guide.md`

## 开发计划
- 完善学生基本信息管理
- 实现Redis版学号池
- 改进前端页面交互
- 实现学生选课功能

## 注意事项
- 这是一个学习项目，功能持续开发中
- 代码提交前请先本地测试
- 遵循项目Git提交规范 