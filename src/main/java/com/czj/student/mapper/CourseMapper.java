package com.czj.student.mapper;

import com.czj.student.model.entity.Course;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 课程Mapper接口
 */
public interface CourseMapper {
    /**
     * 根据ID查询课程
     */
    Course selectById(@Param("cid") Long cid);

    /**
     * 查询所有课程
     */
    List<Course> selectAll();

    /**
     * 新增课程
     */
    int insert(Course course);

    /**
     * 更新课程
     */
    int update(Course course);

    /**
     * 删除课程
     */
    int delete(@Param("cid") Long cid);
} 