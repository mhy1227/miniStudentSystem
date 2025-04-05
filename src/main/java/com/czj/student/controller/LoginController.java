package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.vo.LoginVO;
import com.czj.student.model.vo.LoginUserVO;
import com.czj.student.service.LoginService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@RequestBody @Valid LoginVO loginVO, HttpSession session) {
        LoginUserVO loginUserVO = loginService.login(loginVO, session);
        return ApiResponse.success(loginUserVO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        loginService.logout(session);
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