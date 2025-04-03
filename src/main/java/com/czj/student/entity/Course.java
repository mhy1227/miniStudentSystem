package com.czj.student.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程实体类
 */
@Data
public class Course {
    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 学分
     */
    private BigDecimal credit;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
} 