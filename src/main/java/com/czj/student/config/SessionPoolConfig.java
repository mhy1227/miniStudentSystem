package com.czj.student.config;

import com.czj.student.session.pool.SessionPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.servlet.ServletContext;
import org.springframework.web.context.ServletContextAware;

/**
 * 会话池配置类
 */
@Configuration
public class SessionPoolConfig implements ServletContextAware {
    
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    @Bean
    public SessionPool sessionPool() {
        // 从web.xml读取配置参数
        int maxTotal = getIntParameter("session.pool.maxTotal", 100);
        int maxIdle = getIntParameter("session.pool.maxIdle", 20);
        int minIdle = getIntParameter("session.pool.minIdle", 5);
        long maxWaitMillis = getLongParameter("session.pool.maxWaitMillis", 5000L);
        long sessionTimeout = getLongParameter("session.pool.sessionTimeout", 1800000L);
        
        return new SessionPool(maxTotal, maxIdle, minIdle, maxWaitMillis, sessionTimeout);
    }
    
    private int getIntParameter(String name, int defaultValue) {
        String value = servletContext.getInitParameter(name);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
    
    private long getLongParameter(String name, long defaultValue) {
        String value = servletContext.getInitParameter(name);
        return value != null ? Long.parseLong(value) : defaultValue;
    }
} 