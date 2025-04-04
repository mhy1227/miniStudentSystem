package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.entity.Course;
import com.czj.student.service.CourseService;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 课程管理控制器
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    
    private static final Logger log = LoggerFactory.getLogger(CourseController.class);
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 分页查询课程列表
     */
    @GetMapping
    public ApiResponse<PageResult<Course>> list(Course course, @Valid PageRequest pageRequest) {
        log.info("开始查询课程列表，查询参数：course={}, pageRequest={}", course, pageRequest);
        PageResult<Course> result = courseService.listCourses(course, pageRequest);
        log.info("查询课程列表成功，总记录数：{}", result.getTotal());
        return ApiResponse.success(result);
    }
    
    /**
     * 根据ID查询课程
     */
    @GetMapping("/{cid}")
    public ApiResponse<Course> getById(@PathVariable Long cid) {
        Course course = courseService.getCourseById(cid);
        return ApiResponse.success(course);
    }
    
    /**
     * 根据课程编号查询课程
     */
    @GetMapping("/no/{courseNo}")
    public ApiResponse<Course> getByCourseNo(@PathVariable String courseNo) {
        Course course = courseService.getCourseByCourseNo(courseNo);
        return ApiResponse.success(course);
    }
    
    /**
     * 新增课程
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid Course course) {
        courseService.addCourse(course);
        log.info("新增课程成功，courseNo={}", course.getCourseNo());
        return ApiResponse.success();
    }
    
    /**
     * 更新课程信息
     */
    @PutMapping("/{cid}")
    public ApiResponse<Void> update(@PathVariable Long cid, @RequestBody @Valid Course course) {
        course.setCid(cid);
        courseService.updateCourse(course);
        log.info("更新课程成功，cid={}", cid);
        return ApiResponse.success();
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{cid}")
    public ApiResponse<Void> delete(@PathVariable Long cid) {
        courseService.deleteCourse(cid);
        log.info("删除课程成功，cid={}", cid);
        return ApiResponse.success();
    }
} 

