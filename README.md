# 学生信息管理系统

## 项目简介
一个基于SpringMVC的学生管理系统，目前已实现登录功能、会话管理、学号池和分页查询等核心组件，主要用于学习和演示目的。

## 项目目标
- 实现基础的学生信息管理功能
- 学习SpringMVC框架的开发流程
- 实践会话管理和资源池化设计
- 理解MVC设计模式和RESTful API设计

## 已完成功能
- **用户登录与会话管理**
  - 基本的登录认证
  - 会话状态维护
  - 异地登录检测功能
  
- **学号池管理系统**
  - 基础的学号生成与分配
  - 学号回收与重用机制
  - 线程安全的并发处理

- **高性能分页查询**
  - 基于AOP的分页查询缓存
  - 注解驱动的缓存管理
  - 关键字搜索功能
  - 缓存自动失效机制
  
- **文件上传功能设计**
  - 安全的文件上传机制设计
  - 文件命名策略
  - 安全验证方案

## 待实现功能
- 学生信息的增删改查
- 课程管理功能
- 选课管理功能
- 成绩管理功能
- 权限管理系统

## 技术架构
- 后端：Spring MVC、MyBatis
- 前端：HTML、CSS、JavaScript、Bootstrap
- 数据库：MySQL
- 缓存：自定义内存缓存、AOP实现
- 会话管理：基于池化的会话管理系统

## 文档目录
- **核心功能文档**
  - [异地登录功能测试指南](docs/login/session_test_guide.md)
  - [学号池设计文档](docs/pool/student_number_pool_design.md)
  - [分页功能实现文档](docs/features/pagination/pagination_implementation.md)
  - [文件上传设计文档](docs/features/file-upload/file_upload_design.md)
  
- **规划文档**
  - [系统未来扩展规划](docs/roadmap/future_expansion_plan.md)

## 技术组件
- **会话池（Session Pool）**
  - 管理用户会话状态
  - 实现简单的异地登录检测
  - 会话超时自动清理
  
- **学号池（Sno Pool）**
  - 内存存储的学号资源池
  - 学号的分配与回收
  - 并发安全设计

- **查询缓存池（Query Pool）**
  - 基于内存的查询结果缓存
  - AOP切面实现自动缓存
  - 缓存分组与自动失效
  - 支持性能监控统计

## 技术栈
- **后端**
  - Spring MVC 5.3.x
  - MyBatis 3.5.x
  - MySQL 8.0
  - Druid 连接池
  - AOP切面编程
  
- **前端**
  - HTML + CSS
  - JavaScript + jQuery
  - Bootstrap 基础组件
  - 响应式分页控件
  
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
│   │   │       ├── annotation/   # 自定义注解
│   │   │       ├── aspect/       # AOP切面
│   │   │       ├── common/       # 通用工具
│   │   │       ├── controller/   # 控制器
│   │   │       ├── dao/          # 数据访问
│   │   │       ├── interceptor/  # 拦截器
│   │   │       ├── model/        # 数据模型
│   │   │       │   ├── dto/      # 数据传输对象
│   │   │       │   ├── entity/   # 实体对象
│   │   │       │   └── vo/       # 视图对象
│   │   │       ├── service/      # 服务层
│   │   │       ├── session/      # 会话管理
│   │   │       ├── snopool/      # 学号池
│   │   │       └── util/         # 工具类
│   │   │           └── pool/     # 缓存池
│   │   ├── resources/            # 配置文件
│   │   └── webapp/               # Web资源
│   └── test/                     # 测试代码
└── docs/                         # 文档
    ├── features/                 # 功能文档
    │   └── pagination/           # 分页功能文档
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

## 运行指南
1. 克隆项目到本地
2. 配置数据库连接（application.properties）
3. 部署到Tomcat或其他Servlet容器
4. 访问 http://localhost:8080/login.html

## 开发指南

### 分支管理
- `