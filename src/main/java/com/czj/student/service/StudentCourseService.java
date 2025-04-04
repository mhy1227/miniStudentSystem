package com.czj.student.service;

import com.czj.student.model.entity.StudentCourse;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;

import java.util.List;

/**
 * 选课服务接口
 */
public interface StudentCourseService {
    
    /**
     * 学生选课
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     */
    void selectCourse(Long studentSid, Long courseCid, String semester);
    
    /**
     * 退选课程
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     */
    void dropCourse(Long studentSid, Long courseCid, String semester);
    
    /**
     * 查询学生的选课列表
     * @param studentSid 学生ID
     * @param semester 学期
     * @return 选课列表
     */
    List<StudentCourse> getStudentCourses(Long studentSid, String semester);
    
    /**
     * 查询课程的选课学生列表
     * @param courseCid 课程ID
     * @param semester 学期
     * @return 选课学生列表
     */
    List<StudentCourse> getCourseStudents(Long courseCid, String semester);
    
    /**
     * 分页查询选课记录
     * @param studentCourse 查询条件
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    PageResult<StudentCourse> listStudentCourses(StudentCourse studentCourse, PageRequest pageRequest);
} 