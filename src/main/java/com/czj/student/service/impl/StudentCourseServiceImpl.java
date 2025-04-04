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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    @Override
    @Transactional
    public void updateRegularScore(Long studentSid, Long courseCid, String semester, BigDecimal regularScore) {
        // 参数校验
        validateParams(studentSid, courseCid, semester);
        if (regularScore == null) {
            throw new IllegalArgumentException("平时成绩不能为空");
        }
        if (regularScore.compareTo(BigDecimal.ZERO) < 0 || regularScore.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("成绩必须在0-100之间");
        }

        // 查询选课记录
        StudentCourse studentCourse = studentCourseMapper.selectOne(studentSid, courseCid, semester);
        if (studentCourse == null) {
            throw new RuntimeException("未找到选课记录");
        }

        // 更新平时成绩
        studentCourse.setRegularScore(regularScore);
        studentCourse.setRegularScoreDate(new Date());
        studentCourse.setStatus(2); // 更新状态为已录入平时成绩

        // 保存更新
        int rows = studentCourseMapper.update(studentCourse);
        if (rows != 1) {
            throw new RuntimeException("更新平时成绩失败");
        }
    }

    @Override
    @Transactional
    public void updateExamScore(Long studentSid, Long courseCid, String semester, BigDecimal examScore) {
        // 参数校验
        validateParams(studentSid, courseCid, semester);
        if (examScore == null) {
            throw new IllegalArgumentException("考试成绩不能为空");
        }
        if (examScore.compareTo(BigDecimal.ZERO) < 0 || examScore.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("成绩必须在0-100之间");
        }

        // 查询选课记录
        StudentCourse studentCourse = studentCourseMapper.selectOne(studentSid, courseCid, semester);
        if (studentCourse == null) {
            throw new RuntimeException("未找到选课记录");
        }

        // 检查是否已录入平时成绩
        if (studentCourse.getStatus() < 2) {
            throw new RuntimeException("请先录入平时成绩");
        }

        // 更新考试成绩
        studentCourse.setExamScore(examScore);
        studentCourse.setExamScoreDate(new Date());
        studentCourse.setStatus(3); // 更新状态为已录入考试成绩

        // 保存更新
        int rows = studentCourseMapper.update(studentCourse);
        if (rows != 1) {
            throw new RuntimeException("更新考试成绩失败");
        }

        // 自动计算最终成绩
        calculateFinalScore(studentSid, courseCid, semester);
    }

    @Override
    @Transactional
    public void calculateFinalScore(Long studentSid, Long courseCid, String semester) {
        // 查询选课记录
        StudentCourse studentCourse = studentCourseMapper.selectOne(studentSid, courseCid, semester);
        if (studentCourse == null) {
            throw new RuntimeException("未找到选课记录");
        }

        // 检查是否已录入所有成绩
        if (studentCourse.getStatus() < 3) {
            throw new RuntimeException("请先录入平时成绩和考试成绩");
        }

        // 计算最终成绩（平时成绩占40%，考试成绩占60%）
        BigDecimal regularScore = studentCourse.getRegularScore().multiply(new BigDecimal("0.4"));
        BigDecimal examScore = studentCourse.getExamScore().multiply(new BigDecimal("0.6"));
        BigDecimal finalScore = regularScore.add(examScore);

        // 更新最终成绩
        studentCourse.setFinalScore(finalScore);
        studentCourse.setFinalScoreDate(new Date());
        studentCourse.setStatus(4); // 更新状态为已完成

        // 保存更新
        int rows = studentCourseMapper.update(studentCourse);
        if (rows != 1) {
            throw new RuntimeException("更新最终成绩失败");
        }
    }

    @Override
    public List<StudentCourse> getStudentGrades(Long studentSid, String semester) {
        if (studentSid == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        return studentCourseMapper.selectByStudent(studentSid, semester);
    }

    @Override
    public List<StudentCourse> getCourseGrades(Long courseCid, String semester) {
        if (courseCid == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        return studentCourseMapper.selectByCourse(courseCid, semester);
    }

    @Override
    public Map<String, Object> getCourseGradeStats(Long courseCid, String semester) {
        List<StudentCourse> grades = getCourseGrades(courseCid, semester);
        Map<String, Object> stats = new HashMap<>();
        
        if (grades.isEmpty()) {
            stats.put("avgScore", 0);
            stats.put("maxScore", 0);
            stats.put("minScore", 0);
            stats.put("passRate", 0);
            return stats;
        }

        // 计算统计数据
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("100");
        int passCount = 0;
        int totalCount = 0;

        for (StudentCourse grade : grades) {
            if (grade.getFinalScore() != null) {
                BigDecimal score = grade.getFinalScore();
                totalScore = totalScore.add(score);
                maxScore = maxScore.max(score);
                minScore = minScore.min(score);
                if (score.compareTo(new BigDecimal("60")) >= 0) {
                    passCount++;
                }
                totalCount++;
            }
        }

        // 设置统计结果
        stats.put("avgScore", totalCount > 0 ? totalScore.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) : 0);
        stats.put("maxScore", maxScore);
        stats.put("minScore", minScore);
        stats.put("passRate", totalCount > 0 ? (double) passCount / totalCount : 0);

        return stats;
    }
} 