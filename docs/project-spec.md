# 项目规范文档

## 1. 项目介绍
基于Spring MVC的学生信息管理系统，采用标准三层架构，遵循SOLID/DRY/KISS/YAGNI原则。

## 2. 技术栈
- 核心框架：Spring MVC 5.x
- ORM框架：MyBatis 3.5+
- 数据库：MySQL
- 必需依赖：
  - Spring Web MVC
  - MyBatis-Spring
  - HikariCP
  - Lombok

## 3. 架构规范

### 3.1 分层结构
- Controller → Service → Mapper
- 严格禁止跨层调用

### 3.2 Controller层规范
- API控制器使用`@RestController`
- 统一返回`ResponseEntity<ApiResponse<T>>`
- 参数验证使用`@Valid`
- 禁止直接访问Mapper

### 3.3 Service层规范
- 接口与实现分离
- 使用`@Transactional(rollbackFor = Exception.class)`
- 必须进行日志记录
- 返回DTO而非Entity对象

### 3.4 Mapper层规范
- 接口添加`@Mapper`注解
- 复杂SQL使用XML配置
- 使用`#{}`进行参数绑定，禁止字符串拼接
- 动态SQL使用`<if>`和`<foreach>`

### 3.5 DTO规范
- 请求对象：XxxCreateDTO/XxxUpdateDTO
- 响应对象：XxxDTO/XxxDetailDTO
- 使用Java Record定义（如果使用JDK 17）

## 4. 强制要求
1. 所有数据库操作必须通过Mapper
2. 事务管理必须在Service层
3. 参数校验使用JSR-303注解
4. 异常处理使用全局ControllerAdvice
5. 禁止超过3层的循环嵌套

## 5. 推荐实践
- 使用PageHelper处理分页
- 复杂查询优先使用XML配置
- 采用驼峰命名映射
- 批量操作使用`@BatchInsert`

## 6. 响应格式
```java
public record ApiResponse<T>(String code, String message, T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", "Success", data);
    }
}
```

## 7. 异常处理
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(...) {
        // 处理验证异常
    }
}
```

## 8. 项目结构

student-management/
├── docs/                          # 项目文档
│   ├── project-spec.md           # 项目规范文档
│   └── api-doc.md                # API接口文档
├── src/
│   ├── main/
│   │   ├── java/                 # Java源代码
│   │   │   └── com/czj/student/
│   │   │       ├── controller/   # 控制器层：处理请求响应
│   │   │       ├── service/      # 业务层：处理业务逻辑
│   │   │       ├── dao/          # 数据访问层：操作数据库
│   │   │       ├── model/        # 实体类：数据模型
│   │   │       └── util/         # 工具类：通用工具
│   │   ├── resources/            # 配置文件目录
│   │   │   ├── db/              # 数据库相关脚本
│   │   │   └── *.xml/*.properties # 框架配置文件
│   │   └── webapp/              # Web资源
│   │       ├── WEB-INF/         # Web配置和视图
│   │       └── static/          # 静态资源
│   └── test/                    # 测试代码
└── pom.xml                      # 项目依赖管理

## 9. 开发规范

### 9.1 命名规范
- 包名：全小写，如 `com.example.student`
- 类名：大驼峰，如 `StudentController`
- 方法名：小驼峰，如 `getStudentById`
- 变量名：小驼峰，如 `studentList`
- 常量名：全大写下划线，如 `MAX_SIZE`
- 数据库表名：小写下划线，如 `student_info`

### 9.2 代码规范
- 代码缩进：4个空格
- 编码格式：UTF-8
- 换行符：LF（Unix）
- 文件末尾：保留一个空行
- 控制器返回统一使用Result对象

### 9.3 注释规范
- 类注释：说明类的用途、作者、日期
- 方法注释：说明方法的功能、参数、返回值
- 业务代码：关键逻辑需要添加注释
- 所有的注释都使用中文

### 9.4 提交规范
- feat: 新功能开发
- fix: 修复bug
- docs: 文档更新
- style: 代码格式化
- refactor: 代码重构
- test: 测试用例
- chore: 其他修改

## 10. 开发流程
1. 拉取最新代码
2. 创建功能分支
3. 开发并测试
4. 提交代码
5. 代码审查
6. 合并到主分支

## 11. 测试规范
- 单元测试覆盖核心业务逻辑
- 接口测试确保API正常工作
- 提交前进行本地测试
- 记录测试用例和结果