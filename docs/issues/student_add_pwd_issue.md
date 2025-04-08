# 学生添加功能密码字段问题分析与解决方案

## 问题描述

在学生信息管理系统中，尝试添加新学生时出现以下错误：

```
Error updating database. Cause: java.sql.SQLException: Field 'pwd' doesn't have a default value
```

错误显示在执行以下SQL语句时发生：

```sql
INSERT INTO student (sno, name, sfzh, gender, major, remark) VALUES (?, ?, ?, ?, ?, ?)
```

## 原因分析

1. **数据库设计问题**：
   - 学生表（student）中的密码字段（pwd）被设置为NOT NULL且没有默认值
   - 插入SQL语句中没有包含密码字段

2. **系统设计问题**：
   - 学生表同时承担了两个职责：
     - 存储学生基本信息（姓名、学号等）
     - 作为用户账户表（含密码字段用于登录）
   - 前端表单只考虑了学生信息的录入，没有考虑账户密码设置

3. **代码实现问题**：
   - 控制器中有两个添加学生的方法：
     - `add()`：使用Student实体，未处理密码
     - `addWithDTO()`：使用StudentDTO，可能已处理密码
   - 调用的是未处理密码的方法

## 解决方案

### 方案1：数据库添加默认值（最简单）

通过SQL语句为密码字段添加默认值：

```sql
-- 修改student表的pwd字段，添加默认值
ALTER TABLE student MODIFY COLUMN pwd VARCHAR(100) NOT NULL DEFAULT '123456';
```

**优点**：
- 实现简单，只需执行一条SQL语句
- 不需要修改代码
- 对现有功能无影响

**缺点**：
- 所有未设置密码的学生都将使用相同的默认密码，存在安全隐患
- 不符合账户安全最佳实践

### 方案2：控制器方法设置默认密码

在控制器的添加学生方法中手动设置默认密码：

```java
@PostMapping
public ApiResponse<Void> add(@RequestBody @Valid Student student) {
    // 设置默认密码
    student.setPwd("123456");
    studentService.addStudent(student);
    log.info("新增学生成功，sno={}", student.getSno());
    return ApiResponse.success();
}
```

**优点**：
- 实现简单，只需修改几行代码
- 不需要修改数据库结构
- 能明确看到密码设置逻辑

**缺点**：
- 默认密码硬编码在代码中
- 所有新学生使用相同密码

### 方案3：使用StudentDTO方法

修改前端代码，调用使用DTO的方法，并传入默认密码：

```javascript
function saveStudent() {
    const sno = document.getElementById('sno').value;
    const name = document.getElementById('name').value;
    const sfzh = document.getElementById('sfzh').value;
    const gender = document.getElementById('gender').value;
    const major = document.getElementById('major').value;
    const remark = document.getElementById('remark').value;
    
    const student = {
        sno: sno,
        name: name,
        sfzh: sfzh,
        gender: gender,
        major: major,
        remark: remark,
        pwd: "123456"  // 添加默认密码
    };
    
    // 使用DTO接口
    const url = document.getElementById('sid').value ? 
        `/api/students/dto/${document.getElementById('sid').value}` : 
        '/api/students/dto';
    
    fetch(url, {
        method: document.getElementById('sid').value ? 'PUT' : 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(student)
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            alert('保存成功');
            loadStudents();
            resetForm();
        } else {
            alert('保存失败：' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('保存请求失败');
    });
}
```

**优点**：
- 使用已有的DTO方法，可能已经有更完善的密码处理
- 不需要修改后端代码和数据库

**缺点**：
- 需要修改前端代码
- 密码仍然是硬编码的

### 方案4：增加密码生成规则

修改服务层，基于学生信息生成初始密码：

```java
@Override
@Transactional
public void addStudent(Student student) {
    // 数据验证
    validateStudent(student);
    
    // 生成默认密码（例如：身份证后6位）
    if (student.getSfzh() != null && student.getSfzh().length() >= 6) {
        String defaultPwd = student.getSfzh().substring(student.getSfzh().length() - 6);
        student.setPwd(defaultPwd);
    } else {
        // 备选默认密码
        student.setPwd("123456");
    }
    
    // 其他逻辑...
    studentMapper.insert(student);
}
```

**优点**：
- 基于学生信息生成唯一密码，安全性更高
- 实现相对简单
- 不需要修改前端和数据库

**缺点**：
- 如果使用身份证号生成密码，可能涉及隐私问题
- 需要通知学生初始密码

### 方案5：长期解决方案 - 分离用户表和学生表

系统重构，将用户账户和学生信息分开管理：

1. 创建用户表：
```sql
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL,
  `related_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);
```

2. 修改学生表，移除密码字段：
```sql
ALTER TABLE student DROP COLUMN pwd;
```

3. 添加学生时，同时创建用户账户：
```java
@Transactional
public void addStudentWithAccount(StudentDTO studentDTO) {
    // 添加学生信息
    Student student = convertToEntity(studentDTO);
    studentMapper.insert(student);
    
    // 创建用户账户
    User user = new User();
    user.setUsername(student.getSno());
    user.setPassword("123456"); // 初始密码
    user.setRole("STUDENT");
    user.setRelatedId(student.getSid());
    userMapper.insert(user);
}
```

**优点**：
- 符合设计最佳实践，职责分离
- 更灵活的权限和账户管理
- 支持一个用户多角色

**缺点**：
- 实现复杂，需要大量修改
- 需要数据迁移
- 超出当前项目范围

## 推荐方案

基于实际情况和开发资源，**方案2**是最平衡的解决方案：
- 实现简单：只需在控制器添加一行代码
- 无需修改数据库
- 无需修改前端
- 可以快速解决当前问题

此方案可以作为短期解决方案，之后根据系统发展可考虑实施更完善的方案4或方案5。

## 后续优化建议

1. 添加密码加密存储，提高安全性
2. 实现学生首次登录强制修改密码功能
3. 后续系统升级时考虑实施方案5，分离用户与学生信息管理 