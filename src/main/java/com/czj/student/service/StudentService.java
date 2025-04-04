package com.czj.student.service;

import org.springframework.stereotype.Service;
import com.czj.student.model.entity.Student;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;

@Service
public interface StudentService {
    /**
     * 分页查询学生列表
     *
     * @param student 查询条件
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    PageResult<Student> listStudents(Student student, PageRequest pageRequest);

    /**
     * 根据ID查询学生
     *
     * @param sid 学生ID
     * @return 学生信息
     */
    Student getStudentById(Long sid);

    /**
     * 根据学号查询学生
     *
     * @param sno 学号
     * @return 学生信息
     */
    Student getStudentByNo(String sno);

    /**
     * 新增学生
     *
     * @param student 学生信息
     */
    void addStudent(Student student);

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     */
    void updateStudent(Student student);

    /**
     * 删除学生
     *
     * @param sid 学生ID
     */
    void deleteStudent(Long sid);
} 
