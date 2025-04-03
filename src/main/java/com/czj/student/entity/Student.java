package com.czj.student.entity;

import lombok.Data;
import java.util.Date;

/**
 * 学生实体类
 */
@Data
public class Student {
    /**
     * 学生ID
     */
    private Long sid;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证号
     */
    private String sfzh;

    /**
     * 性别：M-男，F-女
     */
    private String gender;

    /**
     * 专业
     */
    private String major;

    /**
     * 其他说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
} 