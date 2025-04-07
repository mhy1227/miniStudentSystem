package com.czj.student.service;

import org.springframework.stereotype.Service;
import com.czj.student.model.entity.Student;
import com.czj.student.model.dto.StudentDTO;
import com.czj.student.model.vo.StudentVO;
import com.czj.student.model.vo.PageInfo;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;

@Service
public interface StudentService {
    /**
     * 分页查询学生列表
     *
     * @param student 查询条件
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    PageResult<Student> listStudents(Student student, PageRequest pageRequest);
    
    /**
     * 使用新分页框架查询学生列表
     *
     * @param pageInfo 分页参数
     * @param keyword 关键字查询
     * @return 分页结果
     */
    PageInfo<StudentVO> queryStudentsByPage(PageInfo<StudentVO> pageInfo, String keyword);

    /**
     * 根据ID查询学生
     *
     * @param sid 学生ID
     * @return 学生信息
     */
    Student getStudentById(Long sid);

    /**
     * 根据学号查询学生
     *
     * @param sno 学号
     * @return 学生信息
     */
    Student getStudentByNo(String sno);

    /**
     * 新增学生
     *
     * @param student 学生信息
     */
    void addStudent(Student student);
    
    /**
     * 新增学生(使用DTO)
     *
     * @param studentDTO 学生数据传输对象
     * @return 是否添加成功
     */
    boolean addStudentDTO(StudentDTO studentDTO);

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     */
    void updateStudent(Student student);
    
    /**
     * 更新学生信息(使用DTO)
     *
     * @param studentDTO 学生数据传输对象
     * @return 是否更新成功
     */
    boolean updateStudentDTO(StudentDTO studentDTO);

    /**
     * 删除学生
     *
     * @param sid 学生ID
     */
    void deleteStudent(Long sid);
} 
