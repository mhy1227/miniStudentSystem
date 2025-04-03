student-management/
├── docs/                                  # 项目文档
│   ├── api-doc.md                        # API接口文档
│   ├── dev-guide.md                      # 开发指南
│   ├── project-spec.md                   # 项目规范
│   └── progress.md                       # 开发进度
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/czj/student/
│   │   │       ├── config/              # 配置类
│   │   │       │   ├── DruidConfig.java
│   │   │       │   ├── MyBatisConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/          # 控制器
│   │   │       │   ├── StudentController.java
│   │   │       │   ├── CourseController.java
│   │   │       │   └── GradeController.java
│   │   │       ├── service/            # 服务层
│   │   │       │   ├── impl/          # 服务实现
│   │   │       │   └── interfaces/    # 服务接口
│   │   │       ├── mapper/            # MyBatis接口
│   │   │       │   ├── StudentMapper.java
│   │   │       │   ├── CourseMapper.java
│   │   │       │   └── GradeMapper.java
│   │   │       ├── model/             # 数据模型
│   │   │       │   ├── entity/       # 实体类
│   │   │       │   ├── dto/          # 数据传输对象
│   │   │       │   └── vo/           # 视图对象
│   │   │       ├── common/           # 公共组件
│   │   │       │   ├── ApiResponse.java
│   │   │       │   ├── BaseException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       └── util/             # 工具类
│   │   ├── resources/
│   │   │   ├── mapper/              # MyBatis XML映射文件
│   │   │   │   ├── StudentMapper.xml
│   │   │   │   ├── CourseMapper.xml
│   │   │   │   └── GradeMapper.xml
│   │   │   ├── db/                  # 数据库脚本
│   │   │   │   └── init.sql
│   │   │   ├── application.properties
│   │   │   ├── logback.xml
│   │   │   └── mybatis-config.xml
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── web.xml
│   └── test/
│       └── java/
│           └── com/czj/student/
│               ├── controller/
│               ├── service/
│               └── mapper/
├── .gitignore
├── pom.xml
└── README.md 