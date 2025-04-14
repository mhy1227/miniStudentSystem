package com.czj.student.mapper;

import com.czj.student.model.entity.QuestionTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionTemplateMapper {
    @Select("SELECT * FROM question_template WHERE status = 1")
    List<QuestionTemplate> selectAll();

    @Select("SELECT * FROM question_template WHERE category = #{category} AND status = 1")
    List<QuestionTemplate> selectByCategory(String category);

    @Select("SELECT * FROM question_template WHERE id = #{id}")
    QuestionTemplate selectById(Integer id);
}