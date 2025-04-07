package com.czj.student.model.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生信息展示对象
 */
public class StudentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 学生ID
     */
    private Long sid;

    /**
     * 学号
     */
    private String sno;

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

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    // Getters and Setters
    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSfzh() {
        return sfzh;
    }

    public void setSfzh(String sfzh) {
        this.sfzh = sfzh;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public String toString() {
        return "StudentVO{" +
                "sid=" + sid +
                ", sno='" + sno + '\'' +
                ", name='" + name + '\'' +
                ", sfzh='" + sfzh + '\'' +
                ", gender='" + gender + '\'' +
                ", major='" + major + '\'' +
                ", remark='" + remark + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", lastLoginTime=" + lastLoginTime +
                '}';
    }
} 