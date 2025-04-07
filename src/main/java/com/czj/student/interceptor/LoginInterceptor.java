package com.czj.student.interceptor;

import com.czj.student.common.ApiResponse;
import com.czj.student.common.LoginConstants;
import com.czj.student.model.vo.LoginUserVO;
import com.czj.student.session.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Resource
    private SessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取当前请求路径
        String requestURI = request.getRequestURI();
        
        // 2. 白名单路径直接放行
        if (isWhiteListUrl(requestURI)) {
            return true;
        }
        
        // 3. 检查HttpSession
        HttpSession session = request.getSession(false);
        if (session == null) {
            handleUnauthorized(request, response);
            return false;
        }
        
        // 4. 获取用户信息
        LoginUserVO loginUser = (LoginUserVO) session.getAttribute(LoginConstants.SESSION_USER_KEY);
        if (loginUser == null) {
            handleUnauthorized(request, response);
            return false;
        }
        
        // 5. 使用SessionPool验证会话
        if (!sessionManager.isValidSession(loginUser.getSno(), session.getId())) {
            handleUnauthorized(request, response, "您的账号已在其他地方登录");
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理未授权的请求
     */
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response) throws Exception {
        handleUnauthorized(request, response, "未登录或会话已过期");
    }
    
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        if (isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(ApiResponse.error(401, message)));
            writer.flush();
            writer.close();
        } else {
            response.sendRedirect("/login.html");
        }
    }
    
    /**
     * 判断是否是白名单URL
     */
    private boolean isWhiteListUrl(String url) {
        return url.contains("/login.html") ||
               url.contains("/api/auth/login") ||
               url.contains("/api/auth/logout") ||
               url.contains("/js/") ||
               url.contains("/css/") ||
               url.contains("/images/") ||
               url.contains("/favicon.ico");
    }
    
    /**
     * 判断是否是AJAX请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xRequestedWith);
    }
} 