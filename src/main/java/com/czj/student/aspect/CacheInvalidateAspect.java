package com.czj.student.aspect;

import com.czj.student.annotation.CacheInvalidate;
import com.czj.student.util.pool.QueryPool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 缓存失效切面
 */
@Aspect
@Component
public class CacheInvalidateAspect {
    private static final Logger logger = LoggerFactory.getLogger(CacheInvalidateAspect.class);
    
    /**
     * 缓存失效切点
     */
    @Pointcut("@annotation(com.czj.student.annotation.CacheInvalidate)")
    public void cacheInvalidatePointcut() {}
    
    /**
     * 处理缓存失效
     */
    @Around("cacheInvalidatePointcut() && @annotation(cacheInvalidate)")
    public Object handleCacheInvalidate(ProceedingJoinPoint joinPoint, CacheInvalidate cacheInvalidate) throws Throwable {
        // 先执行原方法
        Object result = joinPoint.proceed();
        
        // 清除指定的缓存组
        String[] cacheGroups = cacheInvalidate.cacheGroups();
        if (cacheGroups != null && cacheGroups.length > 0) {
            for (String group : cacheGroups) {
                logger.debug("清除缓存组: {}, 方法: {}", group, joinPoint.getSignature());
                QueryPool.clearCacheGroup(group);
            }
        }
        
        return result;
    }
}