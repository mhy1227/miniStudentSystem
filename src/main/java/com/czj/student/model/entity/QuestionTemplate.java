package com.czj.student.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class QuestionTemplate {
    private Integer id;
    private String question;
    private String category;
    private Integer status;
    private Date createTime;
}