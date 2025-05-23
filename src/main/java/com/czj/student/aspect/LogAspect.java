package com.czj.student.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.czj.student.annotation.Log;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    // 定义切点：拦截所有controller包下的方法
    @Pointcut("execution(* com.czj.student.controller.*.*(..))")
    public void logPointcut() {
    }

    // 定义切点：拦截使用@Log注解的方法
    @Pointcut("@annotation(com.czj.student.annotation.Log)")
    public void logAnnotationPointcut() {
    }

    // 环绕通知
    @Around("logPointcut() || logAnnotationPointcut()")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        // 1. 获取开始时间
        long startTime = System.currentTimeMillis();
        
        // 2. 获取请求信息
        String className = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();
        
        // 3. 获取注解信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        if (logAnnotation != null) {
            logger.info("模块名称: {}", logAnnotation.module());
            logger.info("操作类型: {}", logAnnotation.type());
            logger.info("操作描述: {}", logAnnotation.description());
        }
        
        // 4. 打印请求信息
        logger.info("开始调用: {}.{}", className, methodName);
        logger.info("方法参数: {}", Arrays.toString(args));
        
        // 5. 执行原方法
        Object result = null;
        try {
            result = point.proceed();
            // 6. 打印响应结果
            logger.info("方法返回: {}", result);
        } catch (Exception e) {
            // 7. 打印异常信息
            logger.error("方法异常: {}", e.getMessage());
            throw e;
        } finally {
            // 8. 打印执行时间
            long endTime = System.currentTimeMillis();
            logger.info("执行耗时: {}ms", (endTime - startTime));
        }
        
        return result;
    }
}
