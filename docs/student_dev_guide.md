# Student模块开发指南

## 一、Service层开发
### 1. 创建接口
创建 `src/main/java/com/czj/student/service/StudentService.java`
```java
public interface StudentService {
    // 分页查询学生列表
    PageResult<Student> listStudents(Student student, PageRequest pageRequest);
    
    // 根据ID查询学生
    Student getStudentById(Long id);
    
    // 根据学号查询学生
    Student getStudentByNo(String studentNo);
    
    // 新增学生
    void addStudent(Student student);
    
    // 更新学生信息
    void updateStudent(Student student);
    
    // 删除学生
    void deleteStudent(Long id);
}
```

### 2. 创建实现类
创建 `src/main/java/com/czj/student/service/impl/StudentServiceImpl.java`
```java
@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentMapper studentMapper;
    
    // 实现接口中定义的方法
    // 注意添加参数校验和业务逻辑处理
    // 使用ValidateUtils进行数据验证
}
```

## 二、Controller层开发
创建 `src/main/java/com/czj/student/controller/StudentController.java`
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;
    
    // GET /api/students - 查询学生列表（分页）
    // GET /api/students/{id} - 查询单个学生
    // POST /api/students - 新增学生
    // PUT /api/students/{id} - 更新学生
    // DELETE /api/students/{id} - 删除学生
}
```

## 三、接口规范

### 1. 查询学生列表
- 请求方式：GET
- 请求路径：/api/students
- 请求参数：
  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "studentNo": "可选",
    "name": "可选",
    "gender": "可选",
    "major": "可选"
  }
  ```
- 响应结果：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "total": 100,
      "list": [{
        "id": 1,
        "studentNo": "2021001",
        "name": "张三",
        "gender": "M",
        "major": "计算机科学"
      }],
      "pageNum": 1,
      "pageSize": 10
    }
  }
  ```

### 2. 查询单个学生
- 请求方式：GET
- 请求路径：/api/students/{id}
- 响应结果：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "studentNo": "2021001",
      "name": "张三",
      "sfzh": "320123...",
      "gender": "M",
      "major": "计算机科学",
      "remark": "备注"
    }
  }
  ```

### 3. 新增学生
- 请求方式：POST
- 请求路径：/api/students
- 请求参数：
  ```json
  {
    "studentNo": "必填",
    "name": "必填",
    "sfzh": "必填",
    "gender": "必填",
    "major": "必填",
    "remark": "可选"
  }
  ```

### 4. 更新学生
- 请求方式：PUT
- 请求路径：/api/students/{id}
- 请求参数：同新增，但字段都是可选的

### 5. 删除学生
- 请求方式：DELETE
- 请求路径：/api/students/{id}

## 四、数据验证规则

1. 学号(studentNo)：
   - 必填
   - 长度为8位数字
   - 不能重复

2. 姓名(name)：
   - 必填
   - 长度2-50个字符

3. 身份证号(sfzh)：
   - 必填
   - 符合身份证号格式
   - 不能重复

4. 性别(gender)：
   - 必填
   - 只能是 M 或 F

5. 专业(major)：
   - 必填
   - 长度2-50个字符

## 五、注意事项

1. 所有接口都需要使用 `ApiResponse` 包装返回结果
2. 使用 `@Validated` 进行参数校验
3. 异常统一由 `GlobalExceptionHandler` 处理
4. 查询接口注意性能优化
5. 注意添加适当的注释
6. 遵循 RESTful API 设计规范

## 六、测试用例

在完成开发后，请测试以下场景：
1. 正常新增、修改、删除、查询操作
2. 各种参数校验是否生效
3. 异常情况是否正确处理
4. 分页查询是否正确
5. 并发操作是否有问题 