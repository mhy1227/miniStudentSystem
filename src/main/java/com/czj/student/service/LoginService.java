package com.czj.student.service;

import com.czj.student.model.vo.LoginVO;
import com.czj.student.model.vo.LoginUserVO;

import javax.servlet.http.HttpSession;

/**
 * 登录服务接口
 */
public interface LoginService {
    /**
     * 验证登录凭证
     *
     * @param loginVO 登录参数
     * @throws RuntimeException 验证失败时抛出异常
     */
    void validateCredentials(LoginVO loginVO);

    /**
     * 登录
     *
     * @param loginVO 登录参数
     * @param session 会话
     * @return 登录用户信息
     */
    LoginUserVO login(LoginVO loginVO, HttpSession session);

    /**
     * 退出登录
     *
     * @param session 会话
     */
    void logout(HttpSession session);

    /**
     * 获取当前登录用户
     *
     * @param session 会话
     * @return 登录用户信息，未登录返回null
     */
    LoginUserVO getCurrentUser(HttpSession session);

    /**
     * 检查账号是否被锁定
     *
     * @param sno 学号
     * @param session 会话
     * @return true-已锁定，false-未锁定
     */
    boolean isAccountLocked(String sno, HttpSession session);

    /**
     * 重置密码
     *
     * @param sno 学号
     * @param newPassword 新密码
     */
    void resetPassword(String sno, String newPassword);

    /**
     * 管理员重置用户密码
     *
     * @param sno 学号
     * @param newPassword 新密码
     * @param adminId 管理员ID
     */
    void adminResetPassword(String sno, String newPassword, String adminId);
} 