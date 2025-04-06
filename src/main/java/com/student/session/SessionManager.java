package com.student.session;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 会话管理器，用于处理用户登录会话
 * 实现异地登录检测功能
 */
@Component
public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // 存储用户会话信息
    private static final Map<String, UserSession> sessionMap = new ConcurrentHashMap<>();
    
    // 会话超时时间（30分钟）
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;
    
    /**
     * 用户会话信息类
     */
    private static class UserSession {
        private final String sno;          // 学号
        private final String sessionId;    // 会话ID
        private final String ip;           // 登录IP
        private final Date loginTime;      // 登录时间
        private volatile long lastAccessTime; // 最后访问时间
        
        private UserSession(String sno, String sessionId, String ip) {
            this.sno = sno;
            this.sessionId = sessionId;
            this.ip = ip;
            this.loginTime = new Date();
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        // Getter方法
        public String getSno() { return sno; }
        public String getSessionId() { return sessionId; }
        public String getIp() { return ip; }
        public Date getLoginTime() { return loginTime; }
        public long getLastAccessTime() { return lastAccessTime; }
        
        // 更新最后访问时间
        public void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 处理用户登录
     * @param sno 学号
     * @param sessionId 会话ID
     * @param ip 登录IP
     * @return 登录结果
     */
    public boolean login(String sno, String sessionId, String ip) {
        try {
            // 检查是否已经登录
            UserSession existingSession = sessionMap.get(sno);
            if (existingSession != null) {
                logger.info("用户[{}]在IP[{}]尝试登录，但已在IP[{}]登录", 
                    sno, ip, existingSession.getIp());
                return false;
            }
            
            // 创建新会话
            UserSession newSession = new UserSession(sno, sessionId, ip);
            sessionMap.put(sno, newSession);
            logger.info("用户[{}]从IP[{}]登录成功", sno, ip);
            return true;
            
        } catch (Exception e) {
            logger.error("处理用户[{}]登录时发生异常", sno, e);
            return false;
        }
    }
    
    /**
     * 处理用户登出
     * @param sno 学号
     */
    public void logout(String sno) {
        UserSession session = sessionMap.remove(sno);
        if (session != null) {
            logger.info("用户[{}]从IP[{}]登出", sno, session.getIp());
        }
    }
    
    /**
     * 强制用户下线
     * @param sno 学号
     * @return 是否成功踢出
     */
    public boolean forceLogout(String sno) {
        UserSession session = sessionMap.remove(sno);
        if (session != null) {
            logger.info("强制用户[{}]从IP[{}]下线", sno, session.getIp());
            return true;
        }
        return false;
    }
    
    /**
     * 验证会话是否有效
     * @param sno 学号
     * @param sessionId 会话ID
     * @return 是否有效
     */
    public boolean isValidSession(String sno, String sessionId) {
        UserSession session = sessionMap.get(sno);
        if (session != null) {
            // 检查会话是否超时
            if (System.currentTimeMillis() - session.getLastAccessTime() > SESSION_TIMEOUT) {
                logout(sno);  // 超时自动登出
                return false;
            }
            
            boolean isValid = session.getSessionId().equals(sessionId);
            if (isValid) {
                session.updateAccessTime();
            }
            return isValid;
        }
        return false;
    }
    
    /**
     * 获取用户当前登录的IP
     * @param sno 学号
     * @return IP地址，如果未登录则返回null
     */
    public String getCurrentLoginIp(String sno) {
        UserSession session = sessionMap.get(sno);
        return session != null ? session.getIp() : null;
    }
    
    /**
     * 定时清理过期会话
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)  // 每30分钟执行一次
    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        sessionMap.entrySet().removeIf(entry -> {
            UserSession session = entry.getValue();
            boolean isExpired = now - session.getLastAccessTime() > SESSION_TIMEOUT;
            if (isExpired) {
                logger.info("清理超时会话：用户[{}], IP[{}]", session.getSno(), session.getIp());
            }
            return isExpired;
        });
    }
} 