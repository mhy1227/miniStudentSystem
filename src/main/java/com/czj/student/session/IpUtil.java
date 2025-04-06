package com.czj.student.session;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IP工具类
 */
public class IpUtil {
    private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);
    
    /**
     * 获取请求的IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        String ip = null;
        for (String header : headers) {
            ip = request.getHeader(header);
            if (isValidIpHeader(ip)) {
                break;
            }
        }
        
        if (!isValidIpHeader(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        // 本地测试环境特殊处理
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        
        logger.debug("获取到IP地址: {}", ip);
        return ip;
    }
    
    /**
     * 验证IP地址格式
     * @param ip IP地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 本地地址特殊处理
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        
        // 简单的IPv4地址验证
        String ipv4Regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        return ip.matches(ipv4Regex);
    }
    
    /**
     * 验证IP请求头是否有效
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIpHeader(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
    
    /**
     * 获取IP地址的基本信息
     * @param ip IP地址
     * @return IP信息描述
     */
    public static String getIpAddressInfo(String ip) {
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "本地访问";
        }
        
        // 简单判断是否是内网IP
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || 
            ip.startsWith("172.16.") || ip.startsWith("172.31.")) {
            return "内网访问";
        }
        
        return "外网访问";
    }
} 