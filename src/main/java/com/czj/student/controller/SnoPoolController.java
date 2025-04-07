package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.snopool.SnoPool;
import com.czj.student.snopool.SnoService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 学号池控制器 - 提供学号池的REST API
 */
@RestController
@RequestMapping("/api/sno-pool")
public class SnoPoolController {
    
    @Resource
    private SnoService snoService;
    
    /**
     * 为用户分配学号
     */
    @PostMapping("/assign")
    public ApiResponse<String> assignSno(@RequestParam String userId) {
        try {
            String sno = snoService.assignSnoForUser(userId);
            return ApiResponse.success(sno);
        } catch (Exception e) {
            return ApiResponse.error("分配学号失败: " + e.getMessage());
        }
    }
    
    /**
     * 回收学号
     */
    @PostMapping("/recycle")
    public ApiResponse<Boolean> recycleSno(@RequestParam String sno) {
        try {
            boolean result = snoService.recycleSno(sno);
            return result ? 
                ApiResponse.success(true) : 
                ApiResponse.error("回收失败，学号可能未分配或已被回收");
        } catch (Exception e) {
            return ApiResponse.error("回收学号失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询学号状态
     */
    @GetMapping("/status")
    public ApiResponse<SnoPool.SnoInfo> checkSnoStatus(@RequestParam String sno) {
        try {
            SnoPool.SnoInfo info = snoService.getSnoInfo(sno);
            return info != null ? 
                ApiResponse.success(info) : 
                ApiResponse.error("未找到该学号信息");
        } catch (Exception e) {
            return ApiResponse.error("查询学号状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学号池状态
     */
    @GetMapping("/pool-status")
    public ApiResponse<String> getPoolStatus() {
        try {
            String status = snoService.getPoolStatus();
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error("获取学号池状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查学号是否可用
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkSnoAvailability(@RequestParam String sno) {
        try {
            boolean inUse = snoService.isSnoInUse(sno);
            return ApiResponse.success(!inUse);
        } catch (Exception e) {
            return ApiResponse.error("检查学号可用性失败: " + e.getMessage());
        }
    }
} 