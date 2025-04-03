package com.czj.student.entity;

import lombok.Data;
import java.util.Date;

/**
 * 选课实体类
 */
@Data
public class CourseSelection {
    /**
     * 选课ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 学期
     */
    private String semester;

    /**
     * 创建时间
     */
    private Date createdTime;

    // 扩展字段，用于显示
    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 课程名称
     */
    private String courseName;
} 