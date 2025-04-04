package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.entity.StudentCourse;
import com.czj.student.service.StudentCourseService;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 选课管理控制器
 */
@RestController
@RequestMapping("/api/student-courses")
public class StudentCourseController {
    
    private static final Logger log = LoggerFactory.getLogger(StudentCourseController.class);
    
    @Autowired
    private StudentCourseService studentCourseService;
    
    /**
     * 学生选课
     */
    @PostMapping("/select")
    public ApiResponse<Void> selectCourse(@RequestParam Long studentSid,
                                        @RequestParam Long courseCid,
                                        @RequestParam String semester) {
        studentCourseService.selectCourse(studentSid, courseCid, semester);
        log.info("选课成功，studentSid={}, courseCid={}, semester={}", studentSid, courseCid, semester);
        return ApiResponse.success();
    }
    
    /**
     * 退选课程
     */
    @PostMapping("/drop")
    public ApiResponse<Void> dropCourse(@RequestParam Long studentSid,
                                      @RequestParam Long courseCid,
                                      @RequestParam String semester) {
        studentCourseService.dropCourse(studentSid, courseCid, semester);
        log.info("退课成功，studentSid={}, courseCid={}, semester={}", studentSid, courseCid, semester);
        return ApiResponse.success();
    }
    
    /**
     * 查询学生的选课列表
     */
    @GetMapping("/student/{studentSid}")
    public ApiResponse<List<StudentCourse>> getStudentCourses(@PathVariable Long studentSid,
                                                            @RequestParam(required = false) String semester) {
        List<StudentCourse> courses = studentCourseService.getStudentCourses(studentSid, semester);
        return ApiResponse.success(courses);
    }
    
    /**
     * 查询课程的选课学生列表
     */
    @GetMapping("/course/{courseCid}")
    public ApiResponse<List<StudentCourse>> getCourseStudents(@PathVariable Long courseCid,
                                                           @RequestParam(required = false) String semester) {
        List<StudentCourse> students = studentCourseService.getCourseStudents(courseCid, semester);
        return ApiResponse.success(students);
    }
    
    /**
     * 分页查询选课记录
     */
    @GetMapping
    public ApiResponse<PageResult<StudentCourse>> list(StudentCourse studentCourse,
                                                     @Valid PageRequest pageRequest) {
        PageResult<StudentCourse> result = studentCourseService.listStudentCourses(studentCourse, pageRequest);
        return ApiResponse.success(result);
    }

    /**
     * 录入平时成绩
     */
    @PostMapping("/regular-score")
    public ApiResponse<Void> updateRegularScore(@RequestParam Long studentSid,
                                              @RequestParam Long courseCid,
                                              @RequestParam String semester,
                                              @RequestParam BigDecimal regularScore) {
        studentCourseService.updateRegularScore(studentSid, courseCid, semester, regularScore);
        log.info("录入平时成绩成功，studentSid={}, courseCid={}, semester={}, score={}", 
                studentSid, courseCid, semester, regularScore);
        return ApiResponse.success();
    }

    /**
     * 录入考试成绩
     */
    @PostMapping("/exam-score")
    public ApiResponse<Void> updateExamScore(@RequestParam Long studentSid,
                                           @RequestParam Long courseCid,
                                           @RequestParam String semester,
                                           @RequestParam BigDecimal examScore) {
        studentCourseService.updateExamScore(studentSid, courseCid, semester, examScore);
        log.info("录入考试成绩成功，studentSid={}, courseCid={}, semester={}, score={}", 
                studentSid, courseCid, semester, examScore);
        return ApiResponse.success();
    }

    /**
     * 查询学生成绩列表
     */
    @GetMapping("/grades/student/{studentSid}")
    public ApiResponse<List<StudentCourse>> getStudentGrades(@PathVariable Long studentSid,
                                                           @RequestParam(required = false) String semester) {
        List<StudentCourse> grades = studentCourseService.getStudentGrades(studentSid, semester);
        return ApiResponse.success(grades);
    }

    /**
     * 查询课程成绩列表
     */
    @GetMapping("/grades/course/{courseCid}")
    public ApiResponse<List<StudentCourse>> getCourseGrades(@PathVariable Long courseCid,
                                                          @RequestParam(required = false) String semester) {
        List<StudentCourse> grades = studentCourseService.getCourseGrades(courseCid, semester);
        return ApiResponse.success(grades);
    }

    /**
     * 获取课程成绩统计信息
     */
    @GetMapping("/grades/stats/{courseCid}")
    public ApiResponse<Map<String, Object>> getCourseGradeStats(@PathVariable Long courseCid,
                                                              @RequestParam(required = false) String semester) {
        Map<String, Object> stats = studentCourseService.getCourseGradeStats(courseCid, semester);
        return ApiResponse.success(stats);
    }
} 