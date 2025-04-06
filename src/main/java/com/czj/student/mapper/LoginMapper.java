package com.czj.student.mapper;

import com.czj.student.model.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 登录相关数据库操作
 */
@Mapper
public interface LoginMapper {
    /**
     * 根据学号查询学生信息（包含密码）
     */
    @Select("SELECT * FROM student WHERE sno = #{sno}")
    Student getStudentBySno(@Param("sno") String sno);

    /**
     * 更新登录错误次数
     */
    @Update("UPDATE student SET login_error_count = #{count} WHERE sno = #{sno}")
    int updateLoginErrorCount(@Param("sno") String sno, @Param("count") int count);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE student SET last_login_time = NOW() WHERE sno = #{sno}")
    int updateLastLoginTime(@Param("sno") String sno);
} 