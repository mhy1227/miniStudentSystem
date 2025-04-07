package com.czj.student.snopool;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 学号池测试类
 */
public class SnoPoolTest {
    
    @Test
    public void testSnoAllocation() {
        // 创建学号池
        SnoPool snoPool = new SnoPool();
        
        // 分配学号
        String sno1 = snoPool.allocateSno("user001");
        String sno2 = snoPool.allocateSno("user002");
        
        // 验证学号格式
        assertTrue(sno1.startsWith("XH"));
        assertTrue(sno2.startsWith("XH"));
        
        // 验证学号不同
        assertNotEquals(sno1, sno2);
        
        // 验证学号已分配状态
        assertTrue(snoPool.isSnoAllocated(sno1));
        assertTrue(snoPool.isSnoAllocated(sno2));
        
        // 验证学号信息
        SnoPool.SnoInfo info1 = snoPool.getSnoInfo(sno1);
        assertNotNull(info1);
        assertEquals("user001", info1.getUserId());
        assertTrue(info1.isAllocated());
    }
    
    @Test
    public void testSnoRecycle() {
        SnoPool snoPool = new SnoPool();
        
        // 分配学号
        String sno = snoPool.allocateSno("user001");
        assertTrue(snoPool.isSnoAllocated(sno));
        
        // 回收学号
        boolean recycleResult = snoPool.recycleSno(sno);
        assertTrue(recycleResult);
        
        // 验证回收状态
        assertFalse(snoPool.isSnoAllocated(sno));
        
        // 验证学号信息更新
        SnoPool.SnoInfo info = snoPool.getSnoInfo(sno);
        assertNotNull(info);
        assertFalse(info.isAllocated());
        assertTrue(info.getRecycleTime() > 0);
    }
    
    @Test
    public void testReuseRecycledSno() {
        SnoPool snoPool = new SnoPool();
        
        // 分配学号
        String sno = snoPool.allocateSno("user001");
        
        // 回收学号
        snoPool.recycleSno(sno);
        
        // 再次分配，应该重用回收的学号
        String reusedsno = snoPool.allocateSno("user002");
        assertEquals(sno, reusedsno);
        
        // 验证新的分配信息
        SnoPool.SnoInfo info = snoPool.getSnoInfo(reusedsno);
        assertEquals("user002", info.getUserId());
        assertTrue(info.isAllocated());
    }
    
    @Test
    public void testSnoFormat() {
        SnoPool snoPool = new SnoPool();
        
        // 分配多个学号验证格式
        for (int i = 0; i < 20; i++) {
            String sno = snoPool.allocateSno("user" + i);
            assertTrue("学号格式不正确: " + sno, sno.matches("XH\\d{6}"));
        }
    }
} 