package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.vo.LoginVO;
import com.czj.student.model.vo.LoginUserVO;
import com.czj.student.service.LoginService;
import com.czj.student.session.IpUtil;
import com.czj.student.session.SessionManager;

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
        String ip = IpUtil.getIpAddress(request);
        String sessionId = session.getId();
        
        // 1. 先在SessionPool中检查和创建会话
        if (!sessionManager.login(loginVO.getSno(), sessionId, ip)) {
            return ApiResponse.error("该账号已在其他地方登录");
        }
        
        try {
            // 2. 再执行登录逻辑，设置HttpSession
            LoginUserVO loginUserVO = loginService.login(loginVO, session);
            return ApiResponse.success(loginUserVO);
        } catch (Exception e) {
            // 3. 如果登录失败，清理SessionPool中的会话
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