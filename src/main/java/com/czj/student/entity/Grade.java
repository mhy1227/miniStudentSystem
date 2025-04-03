package com.czj.student.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 成绩实体类
 */
@Data
public class Grade {
    /**
     * 成绩ID
     */
    private Long id;

    /**
     * 成绩编号
     */
    private String gradeNo;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 学期
     */
    private String semester;

    /**
     * 平时成绩
     */
    private BigDecimal regularScore;

    /**
     * 考核成绩
     */
    private BigDecimal examScore;

    /**
     * 最终成绩
     */
    private BigDecimal finalScore;

    /**
     * 添加日期
     */
    private Date gradeDate;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    // 扩展字段，用于显示
    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;
} 