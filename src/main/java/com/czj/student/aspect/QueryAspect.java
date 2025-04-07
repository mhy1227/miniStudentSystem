package com.czj.student.aspect;

import com.czj.student.annotation.CacheQuery;
import com.czj.student.annotation.PageQuery;
import com.czj.student.model.vo.PageInfo;
import com.czj.student.util.pool.QueryPool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询缓存切面
 */
@Aspect
@Component
public class QueryAspect {
    private static final Logger logger = LoggerFactory.getLogger(QueryAspect.class);
    
    /**
     * 分页查询切点
     */
    @Pointcut("@annotation(com.czj.student.annotation.PageQuery)")
    public void pageQueryPointcut() {}
    
    /**
     * 普通查询缓存切点
     */
    @Pointcut("@annotation(com.czj.student.annotation.CacheQuery)")
    public void cacheQueryPointcut() {}
    
    /**
     * 处理分页查询
     */
    @Around("pageQueryPointcut() && @annotation(pageQuery)")
    public Object handlePageQuery(ProceedingJoinPoint joinPoint, PageQuery pageQuery) throws Throwable {
        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        PageInfo<?> pageInfo = null;
        
        // 查找PageInfo参数
        for (Object arg : args) {
            if (arg instanceof PageInfo) {
                pageInfo = (PageInfo<?>) arg;
                break;
            }
        }
        
        // 如果没有PageInfo参数，直接执行原方法
        if (pageInfo == null) {
            logger.warn("分页查询方法缺少PageInfo参数: {}", joinPoint.getSignature());
            return joinPoint.proceed();
        }
        
        // 生成方法键
        String methodKey = QueryPool.generateMethodKey(joinPoint);
        
        // 检查是否有已缓存的数据
        String cacheKey = QueryPool.getCacheKeyByMethod(methodKey);
        if (cacheKey != null && pageInfo.getUuid() == null) {
            pageInfo.setUuid(cacheKey);
            
            // 尝试从缓存获取结果
            PageInfo<?> cachedResult = QueryPool.getPagedResult(cacheKey, pageInfo);
            if (cachedResult != null) {
                logger.debug("分页查询命中缓存: {}", methodKey);
                return cachedResult;
            }
        }
        
        // 缓存未命中，执行原方法
        logger.debug("分页查询未命中缓存，执行原方法: {}", methodKey);
        Object result = joinPoint.proceed();
        
        // 如果返回值不是PageInfo，直接返回
        if (!(result instanceof PageInfo)) {
            return result;
        }
        
        // 生成新的缓存键并保存结果
        if (cacheKey == null) {
            cacheKey = QueryPool.generateCacheKey();
            pageInfo.setUuid(cacheKey);
            QueryPool.putMethodMapping(methodKey, cacheKey);
        }
        
        PageInfo<?> resultPageInfo = (PageInfo<?>) result;
        List<?> allData = resultPageInfo.getRows();
        
        // 保存所有数据到缓存
        if (allData != null && !allData.isEmpty()) {
            QueryPool.putResult(cacheKey, allData, pageQuery.expiration(), pageQuery.cacheGroups());
        }
        
        return result;
    }
    
    /**
     * 处理普通缓存查询
     */
    @Around("cacheQueryPointcut() && @annotation(cacheQuery)")
    public Object handleCacheQuery(ProceedingJoinPoint joinPoint, CacheQuery cacheQuery) throws Throwable {
        // 生成方法键
        String methodKey = QueryPool.generateMethodKey(joinPoint);
        
        // 检查是否有已缓存的数据
        String cacheKey = QueryPool.getCacheKeyByMethod(methodKey);
        if (cacheKey != null) {
            // 尝试从缓存获取结果
            List<?> cachedResult = QueryPool.getList(cacheKey);
            if (cachedResult != null) {
                logger.debug("普通查询命中缓存: {}", methodKey);
                return cachedResult;
            }
        }
        
        // 缓存未命中，执行原方法
        logger.debug("普通查询未命中缓存，执行原方法: {}", methodKey);
        Object result = joinPoint.proceed();
        
        // 如果返回值不是List，直接返回
        if (!(result instanceof List)) {
            return result;
        }
        
        // 生成新的缓存键并保存结果
        if (cacheKey == null) {
            cacheKey = QueryPool.generateCacheKey();
            QueryPool.putMethodMapping(methodKey, cacheKey);
        }
        
        List<?> resultList = (List<?>) result;
        
        // 保存结果到缓存
        if (!resultList.isEmpty()) {
            QueryPool.putResult(cacheKey, resultList, cacheQuery.expiration(), cacheQuery.cacheGroups());
        }
        
        return result;
    }
}