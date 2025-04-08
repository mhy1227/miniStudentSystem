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
- `main`: 主分支，保存稳定版本代码
- `feature/*`: 功能开发分支，如 `feature/file-upload`
- `bugfix/*`: 问题修复分支
- `docs/*`: 文档更新分支

### 代码风格
- 遵循Java代码规范
- 使用4空格缩进
- 类和方法必须有JavaDoc注释
- 变量命名采用驼峰命名法
- 常量使用全大写字母，单词间用下划线分隔

### 提交规范
- 使用约定式提交规范（Conventional Commits）
- 格式: `<类型>: <描述>`
- 常用类型: 
  - `feat`: 新功能
  - `fix`: 错误修复
  - `docs`: 文档更改
  - `style`: 代码格式调整
  - `refactor`: 代码重构
  - `test`: 测试相关
  - `chore`: 构建过程或辅助工具的变动

### 单元测试
- 新增功能需编写对应单元测试
- 测试覆盖率保持在70%以上
- 测试代码放在对应的测试目录下

## 贡献指南
欢迎贡献代码和提出建议，请通过以下步骤参与：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: 添加某功能'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

## 版本历史
- v1.0.0 (2025-04): 首个稳定版本，包含登录、学号池、分页查询和文件上传设计