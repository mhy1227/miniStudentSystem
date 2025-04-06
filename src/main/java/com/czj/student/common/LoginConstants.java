package com.czj.student.common;

/**
 * 登录相关常量
 */
public class LoginConstants {
    /**
     * Session中存储用户信息的key
     */
    public static final String SESSION_USER_KEY = "login_user";

    /**
     * Session中存储错误次数的key
     */
    public static final String SESSION_ERROR_COUNT_KEY = "login_error_count";

    /**
     * Session中存储锁定时间的key
     */
    public static final String SESSION_LOCK_TIME_KEY = "login_lock_time";

    /**
     * 最大登录错误次数
     */
    public static final int MAX_ERROR_COUNT = 3;

    /**
     * 锁定时间（分钟）
     */
    public static final int LOCK_TIME_MINUTES = 1; // 改为1分钟，方便测试
} 