package com.czj.student.model.vo;

import lombok.Data;

/**
 * 登录用户信息
 */
@Data
public class LoginUserVO {
    /**
     * 学号
     */
    private String sno;

    /**
     * 姓名
     */
    private String name;

    /**
     * 专业
     */
    private String major;

    /**
     * 性别
     */
    private String gender;
} 