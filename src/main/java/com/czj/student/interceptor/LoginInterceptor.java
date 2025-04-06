package com.czj.student.interceptor;

import com.czj.student.common.ApiResponse;
import com.czj.student.common.LoginConstants;
import com.czj.student.model.vo.LoginUserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取当前请求路径
        String requestURI = request.getRequestURI();
        
        // 2. 白名单路径直接放行
        if (isWhiteListUrl(requestURI)) {
            return true;
        }
        
        // 3. 检查是否已登录
        LoginUserVO loginUser = (LoginUserVO) request.getSession().getAttribute(LoginConstants.SESSION_USER_KEY);
        if (loginUser != null) {
            return true;
        }
        
        // 4. 未登录处理
        // 4.1 判断是否是AJAX请求
        if (isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(ApiResponse.error(401, "未登录或会话已过期")));
            writer.flush();
            writer.close();
        } else {
            // 4.2 普通请求重定向到登录页
            response.sendRedirect("/login.html");
        }
        
        return false;
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