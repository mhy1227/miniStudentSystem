package com.czj.student.annotation;

import java.lang.annotation.*;

/**
 * 缓存失效注解，用于标记会导致缓存失效的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheInvalidate {
    /**
     * 要失效的缓存组名称
     */
    String[] cacheGroups();
}