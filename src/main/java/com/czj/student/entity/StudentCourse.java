package com.czj.student.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 选课及成绩实体类
 */
@Data
public class StudentCourse {
    /**
     * 学生ID
     */
    private Long studentSid;

    /**
     * 课程ID
     */
    private Long courseCid;

    /**
     * 学期
     */
    private String semester;

    /**
     * 状态：1-已选课，2-已录入平时成绩，3-已录入考试成绩，4-已完成
     */
    private Integer status;

    /**
     * 选课日期
     */
    private Date selectionDate;

    /**
     * 平时成绩
     */
    private BigDecimal regularScore;

    /**
     * 考试成绩
     */
    private BigDecimal examScore;

    /**
     * 最终成绩
     */
    private BigDecimal finalScore;

    /**
     * 平时成绩录入日期
     */
    private Date regularScoreDate;

    /**
     * 考试成绩录入日期
     */
    private Date examScoreDate;

    /**
     * 最终成绩录入日期
     */
    private Date finalScoreDate;

    /**
     * 备注
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

    // 扩展字段，用于显示
    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 学分
     */
    private BigDecimal credit;
} 