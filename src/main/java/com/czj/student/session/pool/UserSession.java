package com.czj.student.session.pool;

import java.util.Date;

/**
 * 增强的会话对象，支持池化
 */
public class UserSession {
    // 基础属性
    private String sno;          // 学号
    private String sessionId;    // 会话ID
    private String ip;          // IP地址
    private Date loginTime;     // 登录时间
    private long lastAccessTime; // 最后访问时间
    
    // 池化相关属性
    private boolean inPool;     // 是否在池中
    private long createTime;    // 创建时间
    private int useCount;       // 使用次数
    
    // Getters and Setters
    public String getSno() {
        return sno;
    }
    
    public void setSno(String sno) {
        this.sno = sno;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public Date getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
    
    public long getLastAccessTime() {
        return lastAccessTime;
    }
    
    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
    
    public boolean isInPool() {
        return inPool;
    }
    
    public void setInPool(boolean inPool) {
        this.inPool = inPool;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public int getUseCount() {
        return useCount;
    }
    
    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }
    
    /**
     * 重置会话状态
     */
    public void reset() {
        this.sno = null;
        this.sessionId = null;
        this.ip = null;
        this.loginTime = null;
        this.lastAccessTime = 0;
        this.useCount = 0;
        this.inPool = false;
    }
    
    /**
     * 更新访问时间
     */
    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
        this.useCount++;
    }
} 