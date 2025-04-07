package com.czj.student.util.pool;

import com.czj.student.model.vo.PageInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 查询结果缓存池
 */
@Component
public class QueryPool {
    private static final Logger logger = LoggerFactory.getLogger(QueryPool.class);
    
    // 缓存数据，键为查询标识，值为查询结果
    private static final ConcurrentHashMap<String, List<?>> queryCache = new ConcurrentHashMap<>();
    
    // 方法签名映射，键为方法信息，值为查询标识
    private static final ConcurrentHashMap<String, String> methodCache = new ConcurrentHashMap<>();
    
    // 缓存组映射，键为组名，值为该组的缓存键集合
    private static final ConcurrentHashMap<String, Set<String>> groupCache = new ConcurrentHashMap<>();
    
    // 缓存时间戳，用于自动过期
    private static final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    
    // 最大缓存条目数
    private static final int MAX_CACHE_SIZE = 1000;
    
    // 缓存访问计数，用于LRU策略
    private static final ConcurrentHashMap<String, AtomicLong> cacheHits = new ConcurrentHashMap<>();
    
    // 统计信息
    private static final AtomicLong hits = new AtomicLong(0);
    private static final AtomicLong misses = new AtomicLong(0);
    private static final AtomicLong evictions = new AtomicLong(0);
    
    /**
     * 从缓存获取分页结果
     * @param cacheKey 缓存键
     * @param pageInfo 分页参数
     * @param <T> 数据类型
     * @return 填充了数据的PageInfo对象，如果缓存未命中则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> PageInfo<T> getPagedResult(String cacheKey, PageInfo<T> pageInfo) {
        List<T> cachedList = (List<T>) queryCache.get(cacheKey);
        if (cachedList == null) {
            misses.incrementAndGet();
            return null;
        }
        
        // 更新访问计数和时间戳
        hits.incrementAndGet();
        recordCacheHit(cacheKey);
        
        // 提取分页数据
        int page = pageInfo.getPage();
        int size = pageInfo.getSize();
        int total = cachedList.size();
        int pages = (total + size - 1) / size;
        
        int fromIndex = (page - 1) * size;
        if (fromIndex >= total) {
            fromIndex = 0;
            page = 1;
        }
        
        int toIndex = Math.min(fromIndex + size, total);
        List<T> pagedData = cachedList.subList(fromIndex, toIndex);
        
        // 设置分页信息
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        pageInfo.setRows(pagedData);
        pageInfo.setPage(page);
        
        return pageInfo;
    }
    
    /**
     * 获取普通缓存数据
     * @param cacheKey 缓存键
     * @param <T> 数据类型
     * @return 缓存的数据列表，未命中则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(String cacheKey) {
        List<T> result = (List<T>) queryCache.get(cacheKey);
        if (result == null) {
            misses.incrementAndGet();
            return null;
        }
        
        hits.incrementAndGet();
        recordCacheHit(cacheKey);
        return result;
    }
    
    /**
     * 存储查询结果
     * @param cacheKey 缓存键
     * @param result 查询结果
     * @param expiration 过期时间(毫秒)
     * @param cacheGroups 缓存组
     * @param <T> 数据类型
     */
    public static <T> void putResult(String cacheKey, List<T> result, long expiration, String[] cacheGroups) {
        // 检查缓存容量
        checkCacheCapacity();
        
        // 存储结果
        queryCache.put(cacheKey, result);
        cacheTimestamps.put(cacheKey, System.currentTimeMillis() + expiration);
        recordCacheHit(cacheKey);
        
        // 关联缓存组
        if (cacheGroups != null && cacheGroups.length > 0) {
            for (String group : cacheGroups) {
                groupCache.computeIfAbsent(group, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                         .add(cacheKey);
            }
        }
        
        logger.debug("缓存数据, 键: {}, 大小: {}, 组: {}", cacheKey, result.size(), 
                     cacheGroups != null ? Arrays.toString(cacheGroups) : "无");
    }
    
    /**
     * 存储方法签名到缓存键的映射
     * @param methodKey 方法签名键
     * @param cacheKey 缓存键
     */
    public static void putMethodMapping(String methodKey, String cacheKey) {
        methodCache.put(methodKey, cacheKey);
    }
    
    /**
     * 根据方法签名获取缓存键
     * @param methodKey 方法签名键
     * @return 缓存键
     */
    public static String getCacheKeyByMethod(String methodKey) {
        return methodCache.get(methodKey);
    }
    
    /**
     * 生成方法签名键
     * @param joinPoint AOP连接点
     * @param additionalParams 附加参数
     * @return 方法签名键
     */
    public static String generateMethodKey(ProceedingJoinPoint joinPoint, Object... additionalParams) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        
        StringBuilder key = new StringBuilder(className)
            .append(".")
            .append(methodName)
            .append("(");
        
        // 添加方法参数
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // 跳过PageInfo参数，因为它会变化
            if (arg instanceof PageInfo) {
                continue;
            }
            if (i > 0) {
                key.append(",");
            }
            key.append(arg == null ? "null" : arg.toString());
        }
        
        // 添加额外参数
        if (additionalParams != null && additionalParams.length > 0) {
            for (Object param : additionalParams) {
                key.append(",").append(param == null ? "null" : param.toString());
            }
        }
        
        key.append(")");
        return key.toString();
    }
    
    /**
     * 生成缓存键
     * @return 缓存键
     */
    public static String generateCacheKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 清除指定缓存组的所有缓存
     * @param cacheGroup 缓存组名称
     */
    public static void clearCacheGroup(String cacheGroup) {
        Set<String> keys = groupCache.get(cacheGroup);
        if (keys != null) {
            int count = 0;
            for (String key : keys) {
                if (queryCache.remove(key) != null) {
                    cacheTimestamps.remove(key);
                    cacheHits.remove(key);
                    count++;
                    evictions.incrementAndGet();
                }
            }
            keys.clear();
            logger.debug("清除缓存组: {}, 共清除{}条缓存", cacheGroup, count);
        }
    }
    
    /**
     * 清除所有缓存
     */
    public static void clearAll() {
        int size = queryCache.size();
        queryCache.clear();
        methodCache.clear();
        groupCache.clear();
        cacheTimestamps.clear();
        cacheHits.clear();
        evictions.addAndGet(size);
        logger.debug("清除所有缓存, 共{}条", size);
    }
    
    /**
     * 定期清理过期缓存，每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public static void cleanExpiredCache() {
        long now = System.currentTimeMillis();
        int count = 0;
        
        for (Iterator<Map.Entry<String, Long>> it = cacheTimestamps.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Long> entry = it.next();
            if (entry.getValue() < now) {
                String key = entry.getKey();
                queryCache.remove(key);
                it.remove();
                cacheHits.remove(key);
                count++;
                evictions.incrementAndGet();
                
                // 从缓存组中移除
                for (Set<String> keys : groupCache.values()) {
                    keys.remove(key);
                }
            }
        }
        
        if (count > 0) {
            logger.debug("定时清理过期缓存, 共清除{}条", count);
        }
    }
    
    /**
     * 获取缓存统计信息
     * @return 统计信息字符串
     */
    public static String getStats() {
        long hitCount = hits.get();
        long missCount = misses.get();
        long total = hitCount + missCount;
        double hitRate = total > 0 ? (double) hitCount / total * 100 : 0;
        
        return String.format("QueryPool统计{缓存数: %d, 命中: %d, 未命中: %d, 清除: %d, 命中率: %.2f%%}",
                queryCache.size(), hitCount, missCount, evictions.get(), hitRate);
    }
    
    /**
     * 记录缓存命中
     */
    private static void recordCacheHit(String cacheKey) {
        cacheHits.computeIfAbsent(cacheKey, k -> new AtomicLong(0)).incrementAndGet();
        cacheTimestamps.put(cacheKey, System.currentTimeMillis() + 30 * 60 * 1000); // 重置过期时间
    }
    
    /**
     * 检查缓存容量，如果超出上限则清理最少使用的条目
     */
    private static void checkCacheCapacity() {
        if (queryCache.size() >= MAX_CACHE_SIZE) {
            // 查找访问次数最低的条目
            String leastUsedKey = null;
            long leastHits = Long.MAX_VALUE;
            
            for (Map.Entry<String, AtomicLong> entry : cacheHits.entrySet()) {
                if (entry.getValue().get() < leastHits) {
                    leastHits = entry.getValue().get();
                    leastUsedKey = entry.getKey();
                }
            }
            
            // 移除访问次数最低的条目
            if (leastUsedKey != null) {
                queryCache.remove(leastUsedKey);
                cacheTimestamps.remove(leastUsedKey);
                cacheHits.remove(leastUsedKey);
                evictions.incrementAndGet();
                
                // 从缓存组中移除
                for (Set<String> keys : groupCache.values()) {
                    keys.remove(leastUsedKey);
                }
                
                logger.debug("缓存容量达到上限, 移除最少使用的条目: {}", leastUsedKey);
            }
        }
    }
}