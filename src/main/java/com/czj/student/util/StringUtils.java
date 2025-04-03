package com.czj.student.util;

import java.util.Collection;

/**
 * 字符串工具类
 */
public class StringUtils {
    
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /**
     * 判断字符串是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
    
    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 截取字符串
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        
        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }
        
        if (end > str.length()) {
            end = str.length();
        }
        
        if (start > end) {
            return "";
        }
        
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        
        return str.substring(start, end);
    }
    
    /**
     * 连接字符串数组
     */
    public static String join(Collection<?> collection, String separator) {
        if (collection == null) {
            return null;
        }
        
        if (separator == null) {
            separator = "";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Object item : collection) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            if (item != null) {
                sb.append(item);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 判断字符串是否包含数字
     */
    public static boolean containsDigit(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断字符串是否全为数字
     */
    public static boolean isDigits(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 移除字符串中的空白字符
     */
    public static String removeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
} 