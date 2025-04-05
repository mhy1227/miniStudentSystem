# 对象模型使用指南

## 一、常见对象模型

### 1. POJO（Plain Old Java Object，普通Java对象）
- 定义：最简单的Java对象，不继承任何类、不实现任何接口
- 特点：
  - 具有无参构造函数
  - 属性私有，提供getter/setter
  - 可序列化
  - 没有业务逻辑
- 示例：
```java
public class StudentPojo {
    private String name;
    private int age;
    
    public StudentPojo() {}  // 无参构造
    
    // getter和setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

### 2. Entity（实体类）
- 定义：与数据库表结构对应的Java对象
- 特点：
  - 映射数据库表结构
  - 通常包含主键
  - 可能包含JPA注解
  - 反映数据持久化需求
- 示例：
```java
@Entity
@Table(name = "t_student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_name")
    private String name;
    
    @Column(name = "create_time")
    private Date createTime;
}
```

### 3. BO（Business Object，业务对象）
- 定义：封装业务逻辑的对象
- 特点：
  - 包含业务规则和逻辑
  - 可能组合多个对象
  - 面向业务处理
  - 通常在Service层使用
- 示例：
```java
public class StudentBO {
    private StudentDTO studentInfo;
    private List<CourseDTO> selectedCourses;
    private GradeBO gradeInfo;
    
    // 业务方法
    public boolean canSelectCourse(CourseDTO course) {
        // 判断是否可以选课的业务逻辑
        return true;
    }
    
    public double calculateGPA() {
        // 计算GPA的业务逻辑
        return 4.0;
    }
}
```

### 4. DTO（Data Transfer Object）
- 定义：在不同层之间传输数据的对象
- 目的：减少调用次数，隔离内部实现
- 特点：
  - 纯数据，没有业务逻辑
  - 可能包含多个领域对象的组合
  - 字段根据传输需求定义
  - 通常双向传输（前端到后端，后端到前端）

### 5. VO（View Object）
- 定义：专门用于展示层的对象
- 目的：适配页面显示需求
- 特点：
  - 针对特定页面或视图定制
  - 只包含显示相关的字段
  - 可能包含显示逻辑（如格式化）
  - 通常是后端到前端的单向传输

## 二、对象模型的使用场景

### 1. 分层使用
- 数据库层：Entity
- 业务层：BO
- 传输层：DTO
- 展示层：VO

### 2. 转换关系
```
数据流向：
Entity <-> BO <-> DTO <-> VO

例如查询流程：
Database -> Entity -> BO -> DTO -> VO -> 前端展示

例如保存流程：
前端请求 -> DTO -> BO -> Entity -> Database
```

### 3. 选择建议
1. 简单业务
   - Entity + DTO 足够
   - 可以省略BO和VO

2. 复杂业务
   - 建议使用完整的分层对象
   - Entity：数据库映射
   - BO：业务逻辑封装
   - DTO：数据传输
   - VO：视图展示

## 三、主要区别

### 1. 使用场景
- DTO：
  - 服务层与控制器层之间传递数据
  - 不同服务之间的数据传输
  - 前后端数据交互的通用格式
- VO：
  - 特定页面的数据展示
  - 针对UI组件的数据结构
  - 专注于前端展示需求

### 2. 数据范围
- DTO：
  - 包含完整的业务数据
  - 排除敏感字段
  - 可能组合多个实体的数据
- VO：
  - 仅包含展示需要的字段
  - 可能包含计算或格式化的字段
  - 针对具体视图定制

### 3. 代码示例
```java
// 实体类
public class Student {
    private Long sid;            // 数据库ID
    private String sno;          // 学号
    private String name;         // 姓名
    private String sfzh;         // 身份证号（敏感信息）
    private String gender;       // 性别
    private String major;        // 专业
    private String remark;       // 备注
    private Date createTime;     // 创建时间
    private Date updateTime;     // 更新时间
}

// DTO：用于数据传输
public class StudentDTO {
    private String sno;          // 学号
    private String name;         // 姓名
    private String gender;       // 性别
    private String major;        // 专业
    private String remark;       // 备注
    // 不包含敏感信息和系统字段
}

// VO：用于列表页面
public class StudentListVO {
    private String sno;          // 学号
    private String name;         // 姓名
    private String gender;       // 性别（已转换：M->男，F->女）
    private String major;        // 专业
}

// VO：用于详情页面
public class StudentDetailVO {
    private String sno;          // 学号
    private String name;         // 姓名
    private String gender;       // 性别（已转换）
    private String major;        // 专业
    private String remark;       // 备注
    private List<CourseVO> courses;  // 关联的课程信息
    private GradeStatVO gradeStats;  // 成绩统计信息
}
```

## 四、使用建议

### 1. 转换方式
1. 手动转换
```java
public StudentDTO convertToDTO(Student student) {
    StudentDTO dto = new StudentDTO();
    dto.setSno(student.getSno());
    dto.setName(student.getName());
    // ... 其他字段转换
    return dto;
}
```

2. 工具转换（如MapStruct）
```java
@Mapper
public interface StudentMapper {
    StudentDTO toDTO(Student student);
    StudentListVO toListVO(StudentDTO dto);
    StudentDetailVO toDetailVO(StudentDTO dto);
}
```

### 2. 最佳实践
1. 命名规范
   - DTO类名以DTO结尾
   - VO类名以VO结尾
   - 方法名表明转换方向

2. 职责分离
   - DTO：专注数据传输
   - VO：专注视图展示
   - 避免混用

3. 转换位置
   - Service层：Entity <-> DTO
   - Controller层：DTO <-> VO

4. 注意事项
   - 避免在DTO/VO中包含业务逻辑
   - 确保敏感数据不被传输
   - 考虑数据验证和安全性
   - 注意性能影响

## 五、重构建议

### 1. 循序渐进
1. 第一步：创建基础DTO
   - 从核心实体开始
   - 先处理最常用的字段
   - 保持简单直接

2. 第二步：应用到简单接口
   - 选择查询接口开始
   - 验证转换逻辑
   - 确认前端兼容性

3. 第三步：扩展到其他接口
   - 逐步覆盖其他接口
   - 处理特殊场景
   - 补充必要的转换逻辑

### 2. 注意事项
1. 保持兼容性
   - 考虑现有接口的调用方
   - 可能需要版本控制
   - 逐步替换旧接口

2. 评估影响
   - 统计需要修改的接口
   - 评估前端改动范围
   - 考虑测试覆盖率

3. 制定计划
   - 确定优先级顺序
   - 设置合理的时间节点
   - 预留充足的测试时间 