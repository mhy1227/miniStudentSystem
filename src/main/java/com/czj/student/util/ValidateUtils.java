package com.czj.student.util;

import java.util.regex.Pattern;

/**
 * 数据校验工具类
 */
public class ValidateUtils {
    
    // 手机号正则表达式
    private static final String MOBILE_REGEX = "^1[3-9]\\d{9}$";
    
    // 邮箱正则表达式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    
    // 身份证号正则表达式（18位）
    private static final String ID_CARD_REGEX = "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$";
    
    // 学号正则表达式（假设学号为年份+4位数字，共8位）
    private static final String STUDENT_NO_REGEX = "^\\d{8}$";
    
    /**
     * 验证手机号
     */
    public static boolean isMobile(String mobile) {
        return isMatch(MOBILE_REGEX, mobile);
    }
    
    /**
     * 验证邮箱
     */
    public static boolean isEmail(String email) {
        return isMatch(EMAIL_REGEX, email);
    }
    
    /**
     * 验证身份证号
     */
    public static boolean isIdCard(String idCard) {
        return isMatch(ID_CARD_REGEX, idCard);
    }
    
    /**
     * 验证学号
     */
    public static boolean isStudentNo(String studentNo) {
        return isMatch(STUDENT_NO_REGEX, studentNo);
    }
    
    /**
     * 验证字符串是否匹配正则表达式
     */
    private static boolean isMatch(String regex, String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return Pattern.matches(regex, input);
    }
    
    /**
     * 验证字符串长度是否在指定范围内
     */
    public static boolean isLengthValid(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * 验证数字是否在指定范围内
     */
    public static boolean isNumberInRange(Number number, Number min, Number max) {
        if (number == null || min == null || max == null) {
            return false;
        }
        double value = number.doubleValue();
        return value >= min.doubleValue() && value <= max.doubleValue();
    }
    
    /**
     * 验证性别（M/F）
     */
    public static boolean isValidGender(String gender) {
        return "M".equals(gender) || "F".equals(gender);
    }
    
    /**
     * 验证成绩是否有效（0-100）
     */
    public static boolean isValidScore(Double score) {
        if (score == null) {
            return false;
        }
        return score >= 0 && score <= 100;
    }
    
    /**
     * 验证学分是否有效（0-10）
     */
    public static boolean isValidCredit(Double credit) {
        if (credit == null) {
            return false;
        }
        return credit >= 0 && credit <= 10;
    }
} 