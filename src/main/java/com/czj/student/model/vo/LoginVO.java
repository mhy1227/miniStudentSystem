package com.czj.student.model.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求参数
 */
@Data
public class LoginVO {
    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    private String sno;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String pwd;
} 