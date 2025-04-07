package com.czj.student.snopool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 学号池 - 管理学号的生成、分配与回收
 */
@Component
public class SnoPool {
    private static final Logger logger = LoggerFactory.getLogger(SnoPool.class);
    
    // 学号前缀
    private static final String SNO_PREFIX = "XH";
    
    // 学号数字部分长度
    private static final int SNO_NUMBER_LENGTH = 6;
    
    // 最大学号序号
    private final AtomicInteger maxSnoNumber = new AtomicInteger(0);
    
    // 学号信息存储
    private final Map<String, SnoInfo> snoMap = new ConcurrentHashMap<>();
    
    // 空闲学号队列
    private final Queue<String> idleSnoQueue = new ConcurrentLinkedQueue<>();
    
    // 用于学号生成的锁
    private final Lock snoLock = new ReentrantLock();
    
    /**
     * 初始化学号池
     */
    @PostConstruct
    public void init() {
        logger.info("初始化学号池");
        // 这里可以从数据库加载已分配的学号信息
        // 或者预生成一批学号放入空闲队列
    }
    
    /**
     * 分配学号
     * @param userId 用户ID
     * @return 分配的学号
     */
    public String allocateSno(String userId) {
        // 1. 优先从空闲队列分配
        String sno = idleSnoQueue.poll();
        
        // 2. 如果没有空闲学号，则生成新学号
        if (sno == null) {
            sno = generateNewSno();
        }
        
        // 3. 更新学号信息
        SnoInfo snoInfo = new SnoInfo(sno, userId, true, System.currentTimeMillis());
        snoMap.put(sno, snoInfo);
        
        logger.info("学号[{}]分配给用户[{}]", sno, userId);
        return sno;
    }
    
    /**
     * 回收学号
     * @param sno 要回收的学号
     * @return 是否回收成功
     */
    public boolean recycleSno(String sno) {
        SnoInfo snoInfo = snoMap.get(sno);
        if (snoInfo != null && snoInfo.isAllocated()) {
            // 1. 更新学号状态
            snoInfo.setAllocated(false);
            snoInfo.setRecycleTime(System.currentTimeMillis());
            
            // 2. 加入空闲队列
            idleSnoQueue.offer(sno);
            
            logger.info("学号[{}]已回收", sno);
            return true;
        }
        return false;
    }
    
    /**
     * 检查学号是否已分配
     * @param sno 学号
     * @return 是否已分配
     */
    public boolean isSnoAllocated(String sno) {
        SnoInfo snoInfo = snoMap.get(sno);
        return snoInfo != null && snoInfo.isAllocated();
    }
    
    /**
     * 获取学号信息
     * @param sno 学号
     * @return 学号信息
     */
    public SnoInfo getSnoInfo(String sno) {
        return snoMap.get(sno);
    }
    
    /**
     * 生成新学号
     * @return 新学号
     */
    private String generateNewSno() {
        snoLock.lock();
        try {
            int number = maxSnoNumber.incrementAndGet();
            return formatSno(number);
        } finally {
            snoLock.unlock();
        }
    }
    
    /**
     * 格式化学号
     * @param number 学号数字部分
     * @return 格式化后的学号
     */
    private String formatSno(int number) {
        String numberStr = String.valueOf(number);
        int paddingZeros = SNO_NUMBER_LENGTH - numberStr.length();
        
        StringBuilder sb = new StringBuilder(SNO_PREFIX);
        for (int i = 0; i < paddingZeros; i++) {
            sb.append('0');
        }
        sb.append(numberStr);
        
        return sb.toString();
    }
    
    /**
     * 获取当前学号池状态
     * @return 状态摘要
     */
    public String getStatus() {
        return String.format("SnoPool{maxNumber=%d, total=%d, idle=%d}",
                maxSnoNumber.get(), snoMap.size(), idleSnoQueue.size());
    }
    
    /**
     * 学号信息类
     */
    public static class SnoInfo {
        private final String sno;            // 学号
        private final String userId;         // 用户ID
        private boolean allocated;          // 是否已分配
        private final long allocateTime;     // 分配时间
        private long recycleTime;           // 回收时间
        
        public SnoInfo(String sno, String userId, boolean allocated, long allocateTime) {
            this.sno = sno;
            this.userId = userId;
            this.allocated = allocated;
            this.allocateTime = allocateTime;
            this.recycleTime = 0;
        }
        
        public String getSno() {
            return sno;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public boolean isAllocated() {
            return allocated;
        }
        
        public void setAllocated(boolean allocated) {
            this.allocated = allocated;
        }
        
        public long getAllocateTime() {
            return allocateTime;
        }
        
        public long getRecycleTime() {
            return recycleTime;
        }
        
        public void setRecycleTime(long recycleTime) {
            this.recycleTime = recycleTime;
        }
        
        @Override
        public String toString() {
            return String.format("SnoInfo{sno='%s', userId='%s', allocated=%s, allocateTime=%d, recycleTime=%d}",
                    sno, userId, allocated, allocateTime, recycleTime);
        }
    }
} 