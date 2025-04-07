# 基础开发指南

## 1. 环境要求
- JDK 1.8
- Maven 3.6+
- MySQL 5.7+
- IDEA

## 2. 如何运行
1. 创建数据库
```sql
create database student_db;
```

2. 导入表结构
```sql
create table student (
    id int primary key auto_increment,
    name varchar(50) not null,
    student_no varchar(20) not null,
    class_name varchar(50)
);
```

3. 运行项目
```bash
mvn tomcat7:run
```

## 3. 功能说明
- 查看学生列表：/students
- 添加学生：/students/add
- 修改学生：/students/edit/{id}
- 删除学生：/students/delete/{id}

## 4. 开发步骤
1. 创建实体类 Student
2. 写 StudentMapper
3. 写 StudentService
4. 写 StudentController
5. 写 JSP 页面 