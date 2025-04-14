package com.czj.student.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SecurityQuestion {
    private Integer id;
    private Integer questionId;
    private String answer;
    private String sno;
    private Integer status;
    private Integer failCount;
    private Date lastFailTime;
    private Date createTime;
    private Date updateTime;
    
    // 非表字段
    private String questionContent;
}