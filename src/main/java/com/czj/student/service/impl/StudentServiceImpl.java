package com.czj.student.service.impl;

import com.czj.student.annotation.CacheInvalidate;
import com.czj.student.annotation.PageQuery;
import com.czj.student.model.dto.StudentDTO;
import com.czj.student.model.entity.Student;
import com.czj.student.model.vo.PageInfo;
import com.czj.student.model.vo.StudentVO;
import com.czj.student.mapper.StudentMapper;
import com.czj.student.service.StudentService;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;
import com.czj.student.util.ValidateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public PageResult<Student> listStudents(Student student, PageRequest pageRequest) {
        // 查询总记录数
        long total = studentMapper.selectCount(student);
        
        // 如果没有记录，直接返回空结果
        if (total == 0) {
            return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), 0, Collections.emptyList());
        }
        
        // 查询数据列表
        List<Student> list = studentMapper.selectList(student);
        
        // 返回分页结果
        return new PageResult<>(pageRequest.getPageNum(), pageRequest.getPageSize(), total, list);
    }
    
    @Override
    @PageQuery(cacheGroups = {"student"})
    public PageInfo<StudentVO> queryStudentsByPage(PageInfo<StudentVO> pageInfo, String keyword) {
        // 查询总数
        int total = studentMapper.countStudentsByKeyword(keyword);
        
        // 如果没有记录，直接返回空结果
        if (total == 0) {
            return pageInfo.of(Collections.emptyList(), 0);
        }
        
        // 计算分页参数
        int offset = pageInfo.getOffset();
        int size = pageInfo.getSize();
        
        // 查询数据
        List<StudentVO> students = studentMapper.queryStudentsByPage(offset, size, keyword);
        
        // 设置分页结果
        return pageInfo.of(students, total);
    }

    @Override
    public Student getStudentById(Long sid) {
        // 参数校验
        if (sid == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        
        // 查询学生信息
        Student student = studentMapper.selectById(sid);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        return student;
    }

    @Override
    public Student getStudentByNo(String sno) {
        // 参数校验
        if (!StringUtils.hasText(sno)) {
            throw new IllegalArgumentException("学号不能为空");
        }
        
        // 查询学生信息
        Student student = studentMapper.selectByStudentNo(sno);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        return student;
    }

    @Override
    @Transactional
    public void addStudent(Student student) {
        // 参数校验
        validateStudent(student);
        
        // 检查学号是否已存在
        Student existingStudent = studentMapper.selectByStudentNo(student.getSno());
        if (existingStudent != null) {
            throw new RuntimeException("学号已存在");
        }
        
        // 检查身份证号是否已存在
        Student existingStudentBySfzh = studentMapper.selectList(new Student() {{
            setSfzh(student.getSfzh());
        }}).stream().findFirst().orElse(null);
        if (existingStudentBySfzh != null) {
            throw new RuntimeException("身份证号已存在");
        }
        
        // 插入学生信息
        int rows = studentMapper.insert(student);
        if (rows != 1) {
            throw new RuntimeException("添加学生失败");
        }
    }
    
    @Override
    @Transactional
    @CacheInvalidate(cacheGroups = {"student"})
    public boolean addStudentDTO(StudentDTO studentDTO) {
        // 参数校验
        if (studentDTO == null) {
            throw new IllegalArgumentException("学生信息不能为空");
        }
        
        // 检查学号是否已存在
        Student existingStudent = studentMapper.selectByStudentNo(studentDTO.getSno());
        if (existingStudent != null) {
            throw new RuntimeException("学号已存在");
        }
        
        // 转换为实体对象
        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);
        
        // 设置创建时间
        student.setCreatedTime(new Date());
        student.setUpdatedTime(new Date());
        
        // 插入学生信息
        int rows = studentMapper.insert(student);
        return rows == 1;
    }

    @Override
    @Transactional
    public void updateStudent(Student student) {
        // 参数校验
        if (student.getSid() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        validateStudent(student);
        
        // 检查学生是否存在
        Student existingStudent = studentMapper.selectById(student.getSid());
        if (existingStudent == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 如果修改了学号，检查新学号是否已存在
        if (!existingStudent.getSno().equals(student.getSno())) {
            Student studentWithSameNo = studentMapper.selectByStudentNo(student.getSno());
            if (studentWithSameNo != null) {
                throw new RuntimeException("学号已存在");
            }
        }
        
        // 如果修改了身份证号，检查新身份证号是否已存在
        if (!existingStudent.getSfzh().equals(student.getSfzh())) {
            Student studentWithSameSfzh = studentMapper.selectList(new Student() {{
                setSfzh(student.getSfzh());
            }}).stream().findFirst().orElse(null);
            if (studentWithSameSfzh != null) {
                throw new RuntimeException("身份证号已存在");
            }
        }
        
        // 更新学生信息
        int rows = studentMapper.update(student);
        if (rows != 1) {
            throw new RuntimeException("更新学生信息失败");
        }
    }
    
    @Override
    @Transactional
    @CacheInvalidate(cacheGroups = {"student"})
    public boolean updateStudentDTO(StudentDTO studentDTO) {
        // 参数校验
        if (studentDTO == null || studentDTO.getSid() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        
        // 检查学生是否存在
        Student existingStudent = studentMapper.selectById(studentDTO.getSid());
        if (existingStudent == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 如果修改了学号，检查新学号是否已存在
        if (!existingStudent.getSno().equals(studentDTO.getSno())) {
            Student studentWithSameNo = studentMapper.selectByStudentNo(studentDTO.getSno());
            if (studentWithSameNo != null && !studentWithSameNo.getSid().equals(studentDTO.getSid())) {
                throw new RuntimeException("学号已存在");
            }
        }
        
        // 转换为实体对象
        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);
        
        // 设置更新时间
        student.setUpdatedTime(new Date());
        
        // 更新学生信息
        int rows = studentMapper.update(student);
        return rows == 1;
    }

    @Override
    @Transactional
    @CacheInvalidate(cacheGroups = {"student"})
    public void deleteStudent(Long sid) {
        // 参数校验
        if (sid == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        
        // 检查学生是否存在
        Student student = studentMapper.selectById(sid);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // TODO: 检查学生是否有关联的选课记录，如果有则不能删除
        
        // 删除学生信息
        int rows = studentMapper.deleteById(sid);
        if (rows != 1) {
            throw new RuntimeException("删除学生失败");
        }
    }
    
    /**
     * 校验学生信息
     */
    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("学生信息不能为空");
        }
        
        // 校验学号
        if (!StringUtils.hasText(student.getSno())) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (!student.getSno().matches("^XH\\d{6}$")) {
            throw new IllegalArgumentException("学号格式不正确，应为XH开头加6位数字");
        }
        
        // 校验姓名
        if (!StringUtils.hasText(student.getName())) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (!ValidateUtils.isLengthValid(student.getName(), 2, 30)) {
            throw new IllegalArgumentException("姓名长度应在2-30个字符之间");
        }
        
        // 校验身份证号
        if (!StringUtils.hasText(student.getSfzh())) {
            throw new IllegalArgumentException("身份证号不能为空");
        }
        if (!ValidateUtils.isIdCard(student.getSfzh())) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }
        
        // 校验性别
        if (!StringUtils.hasText(student.getGender())) {
            throw new IllegalArgumentException("性别不能为空");
        }
        if (!ValidateUtils.isValidGender(student.getGender())) {
            throw new IllegalArgumentException("性别只能是M(男)或F(女)");
        }
    }
}

