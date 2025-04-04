package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.entity.Student;
import com.czj.student.service.StudentService;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    
    @Autowired
    private StudentService studentService;
    
    /**
     * 分页查询学生列表
     */
    @GetMapping
    public ApiResponse<PageResult<Student>> list(Student student, @Valid PageRequest pageRequest) {
        log.info("开始查询学生列表，查询参数：student={}, pageRequest={}", student, pageRequest);
        try {
            PageResult<Student> result = studentService.listStudents(student, pageRequest);
            log.info("查询学生列表成功，总记录数：{}", result.getTotal());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询学生列表失败", e);
            return ApiResponse.error("查询学生列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询学生
     */
    @GetMapping("/{sid}")
    public ApiResponse<Student> getById(@PathVariable Long sid) {
        Student student = studentService.getStudentById(sid);
        return ApiResponse.success(student);
    }
    
    /**
     * 根据学号查询学生
     */
    @GetMapping("/no/{sno}")
    public ApiResponse<Student> getByNo(@PathVariable String sno) {
        Student student = studentService.getStudentByNo(sno);
        return ApiResponse.success(student);
    }
    
    /**
     * 新增学生
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid Student student) {
        studentService.addStudent(student);
        return ApiResponse.success();
    }
    
    /**
     * 更新学生信息
     */
    @PutMapping("/{sid}")
    public ApiResponse<Void> update(@PathVariable Long sid, @RequestBody @Valid Student student) {
        student.setSid(sid);
        studentService.updateStudent(student);
        return ApiResponse.success();
    }
    
    /**
     * 删除学生
     */
    @DeleteMapping("/{sid}")
    public ApiResponse<Void> delete(@PathVariable Long sid) {
        studentService.deleteStudent(sid);
        return ApiResponse.success();
    }
}
