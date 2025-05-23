package com.czj.student.annotation;

import java.lang.annotation.*;

/**
 * 分页查询注解，用于标记需要缓存的分页查询方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageQuery {
    /**
     * 缓存组名称，用于批量清除缓存
     */
    String[] cacheGroups() default {};
    
    /**
     * 缓存过期时间(毫秒)，默认30分钟
     */
    long expiration() default 30 * 60 * 1000;
}