package com.czj.student.service;

import org.springframework.stereotype.Service;
import com.czj.student.entity.Student;


@Service
public interface StudentService {
//todo
  //分页查询列表
 // PageResult<Student> listStudents(Student student, PageRequest pageRequest);
Student getStudentById(Long id);

//根据学号查询学生
Student getStudentByNo(String studentNo);

//新增学生
void addStudent(Student student);

//更新学生信息
void updateStudent(Student student);

//删除学生
void deleteStudent(Long id);

} 
