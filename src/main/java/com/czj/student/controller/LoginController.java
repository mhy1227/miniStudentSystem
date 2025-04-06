package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.vo.LoginVO;
import com.czj.student.model.vo.LoginUserVO;
import com.czj.student.service.LoginService;
import com.student.session.SessionManager;
import com.student.session.IpUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Resource
    private LoginService loginService;
    
    @Resource
    private SessionManager sessionManager;

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@RequestBody @Valid LoginVO loginVO, HttpSession session, HttpServletRequest request) {
        // 1. 获取IP地址
        String ip = IpUtil.getIpAddress(request);
        
        // 2. 检查是否允许登录（异地登录检测）
        if (!sessionManager.login(loginVO.getSno(), session.getId(), ip)) {
            return ApiResponse.error("该账号已在其他地方登录，IP: " + sessionManager.getCurrentLoginIp(loginVO.getSno()));
        }
        
        try {
            // 3. 执行原有的登录逻辑
            LoginUserVO loginUserVO = loginService.login(loginVO, session);
            return ApiResponse.success(loginUserVO);
        } catch (Exception e) {
            // 4. 如果登录失败，清除会话记录
            sessionManager.logout(loginVO.getSno());
            throw e;
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        // 1. 获取当前登录用户
        LoginUserVO currentUser = loginService.getCurrentUser(session);
        if (currentUser != null) {
            // 2. 清除会话记录
            sessionManager.logout(currentUser.getSno());
        }
        
        // 3. 执行原有的登出逻辑
        loginService.logout(session);
        
        // 4. 清除客户端cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        
        return ApiResponse.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current-user")
    public ApiResponse<LoginUserVO> getCurrentUser(HttpSession session) {
        LoginUserVO loginUserVO = loginService.getCurrentUser(session);
        return ApiResponse.success(loginUserVO);
    }
} 