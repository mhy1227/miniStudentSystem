package com.czj.student.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.czj.student.service.CourseService;
import com.czj.student.mapper.CourseMapper;
import com.czj.student.model.entity.Course;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;
import com.czj.student.util.ValidateUtils;

import java.util.Collections;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    
    @Autowired
    private CourseMapper courseMapper;

    @Override
    public PageResult<Course> listCourses(Course course, PageRequest pageRequest) {
        // 查询总记录数
        long total = courseMapper.selectCount(course);
        
        // 如果没有记录，直接返回空结果
        if (total == 0) {
            return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), 0, Collections.emptyList());
        }
        
        // 查询数据列表
        List<Course> list = courseMapper.selectList(course);
        
        // 返回分页结果
        return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), total, list);
    }

    @Override
    public Course getCourseById(Long cid) {
        // 参数校验
        if (cid == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        
        // 查询课程信息
        Course course = courseMapper.selectById(cid);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        return course;
    }

    @Override
    public Course getCourseByCourseNo(String courseNo) {
        // 参数校验
        if (!StringUtils.hasText(courseNo)) {
            throw new IllegalArgumentException("课程编号不能为空");
        }
        
        // 查询课程信息
        Course course = courseMapper.selectByCourseNo(courseNo);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        return course;
    }

    @Override
    @Transactional
    public void addCourse(Course course) {
        // 参数校验
        validateCourse(course);
        
        // 检查课程编号是否已存在
        Course existingCourse = courseMapper.selectByCourseNo(course.getCourseNo());
        if (existingCourse != null) {
            throw new RuntimeException("课程编号已存在");
        }
        
        // 插入课程信息
        int rows = courseMapper.insert(course);
        if (rows != 1) {
            throw new RuntimeException("新增课程失败");
        }
    }

    @Override
    @Transactional
    public void updateCourse(Course course) {
        // 参数校验
        if (course.getCid() == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        validateCourse(course);
        
        // 检查课程是否存在
        Course existingCourse = courseMapper.selectById(course.getCid());
        if (existingCourse == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 如果修改了课程编号，检查新编号是否已存在
        if (!existingCourse.getCourseNo().equals(course.getCourseNo())) {
            Course courseWithSameNo = courseMapper.selectByCourseNo(course.getCourseNo());
            if (courseWithSameNo != null) {
                throw new RuntimeException("课程编号已存在");
            }
        }
        
        // 更新课程信息
        int rows = courseMapper.update(course);
        if (rows != 1) {
            throw new RuntimeException("更新课程失败");
        }
    }

    @Override
    @Transactional
    public void deleteCourse(Long cid) {
        // 参数校验
        if (cid == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        
        // 检查课程是否存在
        Course course = courseMapper.selectById(cid);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // TODO: 检查课程是否有关联的选课记录，如果有则不能删除
        
        // 删除课程信息
        int rows = courseMapper.delete(cid);
        if (rows != 1) {
            throw new RuntimeException("删除课程失败");
        }
    }
    
    /**
     * 校验课程信息
     */
    private void validateCourse(Course course) {
        ValidateUtils.notNull(course, "课程信息不能为空");
        ValidateUtils.hasText(course.getCourseNo(), "课程编号不能为空");
        ValidateUtils.hasText(course.getName(), "课程名称不能为空");
        ValidateUtils.notNull(course.getCredit(), "学分不能为空");
        
        if (course.getCredit().compareTo(new java.math.BigDecimal("0")) < 0 
            || course.getCredit().compareTo(new java.math.BigDecimal("10")) > 0) {
            throw new IllegalArgumentException("学分必须在0-10之间");
        }
    }
}
