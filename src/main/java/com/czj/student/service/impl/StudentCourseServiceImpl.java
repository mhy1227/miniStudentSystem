package com.czj.student.service.impl;

import com.czj.student.mapper.StudentCourseMapper;
import com.czj.student.mapper.StudentMapper;
import com.czj.student.mapper.CourseMapper;
import com.czj.student.model.entity.StudentCourse;
import com.czj.student.model.entity.Student;
import com.czj.student.model.entity.Course;
import com.czj.student.service.StudentCourseService;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class StudentCourseServiceImpl implements StudentCourseService {

    @Autowired
    private StudentCourseMapper studentCourseMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private CourseMapper courseMapper;

    @Override
    @Transactional
    public void selectCourse(Long studentSid, Long courseCid, String semester) {
        // 参数校验
        validateParams(studentSid, courseCid, semester);
        
        // 检查学生是否存在
        Student student = studentMapper.selectById(studentSid);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 检查课程是否存在
        Course course = courseMapper.selectById(courseCid);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 检查是否已经选过这门课
        StudentCourse existingSelection = studentCourseMapper.selectOne(studentSid, courseCid, semester);
        if (existingSelection != null) {
            throw new RuntimeException("已经选过这门课程");
        }
        
        // 创建选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentSid(studentSid);
        studentCourse.setCourseCid(courseCid);
        studentCourse.setSemester(semester);
        studentCourse.setStatus(1); // 1-已选课
        studentCourse.setSelectionDate(new Date());
        
        // 保存选课记录
        int rows = studentCourseMapper.insert(studentCourse);
        if (rows != 1) {
            throw new RuntimeException("选课失败");
        }
    }

    @Override
    @Transactional
    public void dropCourse(Long studentSid, Long courseCid, String semester) {
        // 参数校验
        validateParams(studentSid, courseCid, semester);
        
        // 检查选课记录是否存在
        StudentCourse studentCourse = studentCourseMapper.selectOne(studentSid, courseCid, semester);
        if (studentCourse == null) {
            throw new RuntimeException("未选择该课程");
        }
        
        // 检查是否可以退课（已有成绩的课程不能退）
        if (studentCourse.getStatus() > 1) {
            throw new RuntimeException("已录入成绩的课程不能退选");
        }
        
        // 删除选课记录
        int rows = studentCourseMapper.delete(studentSid, courseCid, semester);
        if (rows != 1) {
            throw new RuntimeException("退课失败");
        }
    }

    @Override
    public List<StudentCourse> getStudentCourses(Long studentSid, String semester) {
        if (studentSid == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        return studentCourseMapper.selectByStudent(studentSid, semester);
    }

    @Override
    public List<StudentCourse> getCourseStudents(Long courseCid, String semester) {
        if (courseCid == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        return studentCourseMapper.selectByCourse(courseCid, semester);
    }

    @Override
    public PageResult<StudentCourse> listStudentCourses(StudentCourse studentCourse, PageRequest pageRequest) {
        // 查询总记录数
        long total = studentCourseMapper.selectCount(studentCourse);
        
        // 如果没有记录，直接返回空结果
        if (total == 0) {
            return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), 0, Collections.emptyList());
        }
        
        // 查询数据列表
        List<StudentCourse> list = studentCourseMapper.selectList(studentCourse);
        
        // 返回分页结果
        return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), total, list);
    }
    
    /**
     * 校验参数
     */
    private void validateParams(Long studentSid, Long courseCid, String semester) {
        if (studentSid == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (courseCid == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("学期不能为空");
        }
    }
} 