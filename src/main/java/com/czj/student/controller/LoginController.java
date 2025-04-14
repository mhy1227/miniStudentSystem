package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.vo.LoginVO;
import com.czj.student.model.vo.LoginUserVO;
import com.czj.student.service.LoginService;
import com.czj.student.service.SecurityQuestionService;
import com.czj.student.session.IpUtil;
import com.czj.student.session.SessionManager;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private SecurityQuestionService securityQuestionService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResponse<Object> login(@RequestBody @Valid LoginVO loginVO, HttpSession session, HttpServletRequest request) {
        String ip = IpUtil.getIpAddress(request);
        String sessionId = session.getId();
        
        // 1. 先验证账号密码
        try {
            loginService.validateCredentials(loginVO);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
        
        // 2. 检查是否是首次登录或未设置安全问题
        if (!securityQuestionService.hasSetQuestion(loginVO.getSno())) {
            // 首次登录，直接执行登录流程
            try {
                LoginUserVO loginUserVO = loginService.login(loginVO, session);
                // 强制更新会话
                sessionManager.forceLogin(loginVO.getSno(), sessionId, ip);
                return ApiResponse.success(loginUserVO);
            } catch (Exception e) {
                sessionManager.logout(loginVO.getSno());
                return ApiResponse.error(e.getMessage());
            }
        }
        
        // 3. 检查是否需要安全问题验证
        if (!sessionManager.login(loginVO.getSno(), sessionId, ip)) {
            // 获取用户的安全问题
            List<Map<String, Object>> questions = securityQuestionService.getUserQuestions(loginVO.getSno());
            if (questions.isEmpty()) {
                return ApiResponse.error("该账号已在其他地方登录");
            }
            
            // 返回需要验证的状态和安全问题
            Map<String, Object> data = new HashMap<>();
            data.put("needVerify", true);
            data.put("questions", questions);
            ApiResponse<Object> response = new ApiResponse<>();
            response.setCode(202);
            response.setMessage("需要安全验证");
            response.setData(data);
            return response;
        }
        
        // 4. 执行正常登录流程
        try {
            LoginUserVO loginUserVO = loginService.login(loginVO, session);
            return ApiResponse.success(loginUserVO);
        } catch (Exception e) {
            sessionManager.logout(loginVO.getSno());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 异地登录验证
     */
    @PostMapping("/verify-login")
    public ApiResponse<LoginUserVO> verifyLogin(@RequestBody Map<String, Object> params, 
                                              HttpSession session, 
                                              HttpServletRequest request) {
        String sno = (String) params.get("sno");
        Integer questionId = (Integer) params.get("questionId");
        String answer = (String) params.get("answer");
        
        if (sno == null || questionId == null || answer == null) {
            return ApiResponse.error("参数不完整");
        }
        
        // 1. 验证安全问题
        if (!securityQuestionService.verifyAnswer(sno, questionId, answer)) {
            return ApiResponse.error("验证失败");
        }
        
        // 2. 验证通过，强制登录
        String ip = IpUtil.getIpAddress(request);
        String sessionId = session.getId();
        sessionManager.forceLogin(sno, sessionId, ip);
        
        // 3. 执行登录
        try {
            LoginVO loginVO = new LoginVO();
            loginVO.setSno(sno);
            // 这里密码已经在之前验证过，可以直接从session获取
            LoginUserVO loginUserVO = loginService.getCurrentUser(session);
            if (loginUserVO == null) {
                return ApiResponse.error("登录失败");
            }
            return ApiResponse.success(loginUserVO);
        } catch (Exception e) {
            sessionManager.logout(sno);
            return ApiResponse.error(e.getMessage());
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

    /**
     * 发起重置密码请求（获取安全问题）
     */
    @GetMapping("/reset-password/questions")
    public ApiResponse<List<Map<String, Object>>> getResetQuestions(@RequestParam String sno) {
        // 检查是否设置了安全问题
        if (!securityQuestionService.hasSetQuestion(sno)) {
            return ApiResponse.error("该账号未设置安全问题，请联系管理员重置密码");
        }
        
        // 获取用户的安全问题
        List<Map<String, Object>> questions = securityQuestionService.getUserQuestions(sno);
        if (questions.isEmpty()) {
            return ApiResponse.error("获取安全问题失败");
        }
        
        return ApiResponse.success(questions);
    }

    /**
     * 通过安全问题重置密码
     */
    @PostMapping("/reset-password/verify")
    public ApiResponse<Boolean> resetPasswordByQuestion(@RequestBody Map<String, Object> params) {
        String sno = (String) params.get("sno");
        Integer questionId = (Integer) params.get("questionId");
        String answer = (String) params.get("answer");
        String newPassword = (String) params.get("newPassword");
        
        if (sno == null || questionId == null || answer == null || newPassword == null) {
            return ApiResponse.error("参数不完整");
        }
        
        // 1. 验证安全问题
        if (!securityQuestionService.verifyAnswer(sno, questionId, answer)) {
            return ApiResponse.error("安全问题验证失败");
        }
        
        // 2. 重置密码
        try {
            loginService.resetPassword(sno, newPassword);
            // 3. 清除该用户所有会话
            sessionManager.logout(sno);
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error("重置密码失败：" + e.getMessage());
        }
    }

    /**
     * 管理员重置用户密码
     */
    @PostMapping("/admin/reset-password")
    public ApiResponse<Boolean> adminResetPassword(@RequestBody Map<String, Object> params) {
        String sno = (String) params.get("sno");
        String adminId = (String) params.get("adminId");
        String newPassword = (String) params.get("newPassword");
        
        if (sno == null || adminId == null || newPassword == null) {
            return ApiResponse.error("参数不完整");
        }
        
        try {
            // 这里应该添加管理员权限验证
            loginService.adminResetPassword(sno, newPassword, adminId);
            // 清除该用户所有会话
            sessionManager.logout(sno);
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error("重置密码失败：" + e.getMessage());
        }
    }
} 