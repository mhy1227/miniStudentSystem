package com.czj.student.service;

import com.czj.student.model.entity.StudentCourse;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    
    /**
     * 录入平时成绩
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     * @param regularScore 平时成绩
     */
    void updateRegularScore(Long studentSid, Long courseCid, String semester, BigDecimal regularScore);
    
    /**
     * 录入考试成绩
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     * @param examScore 考试成绩
     */
    void updateExamScore(Long studentSid, Long courseCid, String semester, BigDecimal examScore);
    
    /**
     * 计算并更新最终成绩
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     */
    void calculateFinalScore(Long studentSid, Long courseCid, String semester);
    
    /**
     * 查询学生的成绩列表
     * @param studentSid 学生ID
     * @param semester 学期
     * @return 成绩列表
     */
    List<StudentCourse> getStudentGrades(Long studentSid, String semester);
    
    /**
     * 查询课程的成绩列表
     * @param courseCid 课程ID
     * @param semester 学期
     * @return 成绩列表
     */
    List<StudentCourse> getCourseGrades(Long courseCid, String semester);
    
    /**
     * 统计课程成绩
     * @param courseCid 课程ID
     * @param semester 学期
     * @return 统计结果（包含平均分、最高分、最低分、及格率等）
     */
    Map<String, Object> getCourseGradeStats(Long courseCid, String semester);
} 