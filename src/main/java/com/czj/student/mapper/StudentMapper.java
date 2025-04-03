package com.czj.student.mapper;

import com.czj.student.entity.Student;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 学生Mapper接口
 */
public interface StudentMapper {
    /**
     * 新增学生
     *
     * @param student 学生信息
     * @return 影响行数
     */
    int insert(Student student);

    /**
     * 根据ID删除学生
     *
     * @param sid 学生ID
     * @return 影响行数
     */
    int deleteById(@Param("sid") Long sid);

    /**
     * 更新学生信息
     *
     * @param student 学生信息
     * @return 影响行数
     */
    int update(Student student);

    /**
     * 根据ID查询学生
     *
     * @param sid 学生ID
     * @return 学生信息
     */
    Student selectById(@Param("sid") Long sid);

    /**
     * 根据学号查询学生
     *
     * @param sno 学号
     * @return 学生信息
     */
    Student selectByStudentNo(@Param("sno") String sno);

    /**
     * 查询学生列表
     *
     * @param student 查询条件
     * @return 学生列表
     */
    List<Student> selectList(Student student);

    /**
     * 查询总记录数
     *
     * @param student 查询条件
     * @return 总记录数
     */
    long selectCount(Student student);
} 