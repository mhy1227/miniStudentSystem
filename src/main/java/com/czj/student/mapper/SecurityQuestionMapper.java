package com.czj.student.mapper;

import com.czj.student.model.entity.SecurityQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SecurityQuestionMapper {
    @Insert("INSERT INTO security_question (question_id, answer, sno, status, fail_count) " +
            "VALUES (#{questionId}, #{answer}, #{sno}, #{status}, #{failCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SecurityQuestion question);

    @Update("UPDATE security_question SET answer = #{answer}, status = #{status}, " +
            "fail_count = #{failCount}, last_fail_time = #{lastFailTime} " +
            "WHERE id = #{id}")
    int update(SecurityQuestion question);

    @Select("SELECT * FROM security_question WHERE sno = #{sno}")
    List<SecurityQuestion> selectBySno(String sno);

    @Select("SELECT * FROM security_question WHERE id = #{id}")
    SecurityQuestion selectById(Integer id);

    @Update("UPDATE security_question SET fail_count = #{failCount}, " +
            "last_fail_time = NOW() WHERE id = #{id}")
    int updateFailCount(@Param("id") Integer id, @Param("failCount") Integer failCount);

    @Update("UPDATE security_question SET fail_count = 0, " +
            "last_fail_time = NULL WHERE id = #{id}")
    int resetFailCount(Integer id);
}