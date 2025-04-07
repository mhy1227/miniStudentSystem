package com.czj.student.snopool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * 学号服务 - 对外提供学号管理功能
 */
@Service
public class SnoService {
    private static final Logger logger = LoggerFactory.getLogger(SnoService.class);
    
    @Resource
    private SnoPool snoPool;
    
    /**
     * 为用户分配学号
     * @param userId 用户ID
     * @return 分配的学号
     */
    public String assignSnoForUser(String userId) {
        logger.info("为用户[{}]分配学号", userId);
        return snoPool.allocateSno(userId);
    }
    
    /**
     * 回收指定学号
     * @param sno 学号
     * @return 是否回收成功
     */
    public boolean recycleSno(String sno) {
        logger.info("尝试回收学号[{}]", sno);
        return snoPool.recycleSno(sno);
    }
    
    /**
     * 检查学号是否已分配
     * @param sno 学号
     * @return 是否已分配
     */
    public boolean isSnoInUse(String sno) {
        return snoPool.isSnoAllocated(sno);
    }
    
    /**
     * 获取学号信息
     * @param sno 学号
     * @return 学号信息，如果不存在则返回null
     */
    public SnoPool.SnoInfo getSnoInfo(String sno) {
        SnoPool.SnoInfo info = snoPool.getSnoInfo(sno);
        if (info == null) {
            logger.warn("未找到学号[{}]的信息", sno);
        }
        return info;
    }
    
    /**
     * 获取学号池状态
     * @return 学号池状态摘要
     */
    public String getPoolStatus() {
        return snoPool.getStatus();
    }
} 