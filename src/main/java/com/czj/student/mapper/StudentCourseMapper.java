package com.czj.student.mapper;

import com.czj.student.entity.StudentCourse;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 选课及成绩Mapper接口
 */
public interface StudentCourseMapper {
    /**
     * 新增选课记录
     *
     * @param studentCourse 选课信息
     * @return 影响行数
     */
    int insert(StudentCourse studentCourse);

    /**
     * 更新选课及成绩信息
     *
     * @param studentCourse 选课及成绩信息
     * @return 影响行数
     */
    int update(StudentCourse studentCourse);

    /**
     * 删除选课记录
     *
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     * @return 影响行数
     */
    int delete(@Param("studentSid") Long studentSid, 
               @Param("courseCid") Long courseCid, 
               @Param("semester") String semester);

    /**
     * 查询选课记录
     *
     * @param studentSid 学生ID
     * @param courseCid 课程ID
     * @param semester 学期
     * @return 选课及成绩信息
     */
    StudentCourse selectOne(@Param("studentSid") Long studentSid, 
                          @Param("courseCid") Long courseCid, 
                          @Param("semester") String semester);

    /**
     * 查询学生的选课列表
     *
     * @param studentSid 学生ID
     * @param semester 学期
     * @return 选课列表
     */
    List<StudentCourse> selectByStudent(@Param("studentSid") Long studentSid, 
                                      @Param("semester") String semester);

    /**
     * 查询课程的选课学生列表
     *
     * @param courseCid 课程ID
     * @param semester 学期
     * @return 选课学生列表
     */
    List<StudentCourse> selectByCourse(@Param("courseCid") Long courseCid, 
                                     @Param("semester") String semester);

    /**
     * 根据条件查询选课及成绩列表
     *
     * @param studentCourse 查询条件
     * @return 选课及成绩列表
     */
    List<StudentCourse> selectList(StudentCourse studentCourse);

    /**
     * 查询总记录数
     *
     * @param studentCourse 查询条件
     * @return 总记录数
     */
    long selectCount(StudentCourse studentCourse);
} 