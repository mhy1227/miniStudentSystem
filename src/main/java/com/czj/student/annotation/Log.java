package com.czj.student.annotation;

import java.lang.annotation.*;

/**
 * 自定义日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作类型（如：查询、新增、修改、删除）
     */
    String type() default "";

    /**
     * 描述
     */
    String description() default "";
} 