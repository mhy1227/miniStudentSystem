package com.czj.student.controller;

import com.czj.student.annotation.Log;
import com.czj.student.common.ApiResponse;
import com.czj.student.model.dto.StudentDTO;
import com.czj.student.model.entity.Student;
import com.czj.student.model.vo.PageInfo;
import com.czj.student.model.vo.StudentVO;
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
    @Log(module = "学生管理", type = "查询", description = "分页查询学生列表")
    @GetMapping
    public ApiResponse<PageResult<Student>> list(Student student, @Valid PageRequest pageRequest) {
        log.info("开始查询学生列表，查询参数：student={}, pageRequest={}", student, pageRequest);
        PageResult<Student> result = studentService.listStudents(student, pageRequest);
        log.info("查询学生列表成功，总记录数：{}", result.getTotal());
        return ApiResponse.success(result);
    }
    
    /**
     * 使用新分页框架查询学生列表
     */
    @Log(module = "学生管理", type = "查询", description = "使用新分页框架查询学生列表")
    @GetMapping("/page")
    public ApiResponse<PageInfo<StudentVO>> getStudentsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        log.info("开始分页查询学生列表，参数：page={}, size={}, keyword={}", page, size, keyword);
        PageInfo<StudentVO> pageInfo = new PageInfo<>(page, size);
        pageInfo = studentService.queryStudentsByPage(pageInfo, keyword);
        log.info("分页查询学生列表成功，总记录数：{}", pageInfo.getTotal());
        return ApiResponse.success(pageInfo);
    }
    
    /**
     * 根据ID查询学生
     */
    @Log(module = "学生管理", type = "查询", description = "根据ID查询学生")
    @GetMapping("/{sid}")
    public ApiResponse<Student> getById(@PathVariable Long sid) {
        Student student = studentService.getStudentById(sid);
        return ApiResponse.success(student);
    }
    
    /**
     * 根据学号查询学生
     */
    @Log(module = "学生管理", type = "查询", description = "根据学号查询学生")
    @GetMapping("/no/{sno}")
    public ApiResponse<Student> getByNo(@PathVariable String sno) {
        Student student = studentService.getStudentByNo(sno);
        return ApiResponse.success(student);
    }
    
    /**
     * 新增学生
     */
    @Log(module = "学生管理", type = "新增", description = "新增学生信息")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody @Valid Student student) {
        studentService.addStudent(student);
        log.info("新增学生成功，sno={}", student.getSno());
        return ApiResponse.success();
    }
    
    /**
     * 新增学生(使用DTO)
     */
    @Log(module = "学生管理", type = "新增", description = "新增学生信息(使用DTO)")
    @PostMapping("/dto")
    public ApiResponse<Void> addWithDTO(@RequestBody @Valid StudentDTO studentDTO) {
        boolean success = studentService.addStudentDTO(studentDTO);
        if (success) {
            log.info("新增学生成功，sno={}", studentDTO.getSno());
            return ApiResponse.success();
        } else {
            return ApiResponse.error("新增学生失败");
        }
    }
    
    /**
     * 更新学生信息
     */
    @Log(module = "学生管理", type = "修改", description = "更新学生信息")
    @PutMapping("/{sid}")
    public ApiResponse<Void> update(@PathVariable Long sid, @RequestBody @Valid Student student) {
        student.setSid(sid);
        studentService.updateStudent(student);
        log.info("更新学生成功，sid={}", sid);
        return ApiResponse.success();
    }
    
    /**
     * 更新学生信息(使用DTO)
     */
    @Log(module = "学生管理", type = "修改", description = "更新学生信息(使用DTO)")
    @PutMapping("/dto/{sid}")
    public ApiResponse<Void> updateWithDTO(@PathVariable Long sid, @RequestBody @Valid StudentDTO studentDTO) {
        studentDTO.setSid(sid);
        boolean success = studentService.updateStudentDTO(studentDTO);
        if (success) {
            log.info("更新学生成功，sid={}", sid);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("更新学生失败");
        }
    }
    
    /**
     * 删除学生
     */
    @Log(module = "学生管理", type = "删除", description = "删除学生")
    @DeleteMapping("/{sid}")
    public ApiResponse<Void> delete(@PathVariable Long sid) {
        studentService.deleteStudent(sid);
        log.info("删除学生成功，sid={}", sid);
        return ApiResponse.success();
    }
}
