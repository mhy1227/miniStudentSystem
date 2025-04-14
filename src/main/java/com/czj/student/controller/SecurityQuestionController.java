package com.czj.student.controller;

import com.czj.student.common.ApiResponse;
import com.czj.student.model.entity.QuestionTemplate;
import com.czj.student.service.SecurityQuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 安全问题控制器
 */
@RestController
@RequestMapping("/api/security")
public class SecurityQuestionController {

    @Resource
    private SecurityQuestionService securityQuestionService;

    /**
     * 获取所有问题模板
     */
    @GetMapping("/questions")
    public ApiResponse<List<QuestionTemplate>> getAllQuestions() {
        return ApiResponse.success(securityQuestionService.getAllQuestionTemplates());
    }

    /**
     * 设置安全问题
     */
    @PostMapping("/question")
    public ApiResponse<Boolean> setSecurityQuestion(@RequestBody Map<String, Object> params) {
        String sno = (String) params.get("sno");
        Integer questionId = (Integer) params.get("questionId");
        String answer = (String) params.get("answer");
        
        if (sno == null || questionId == null || answer == null) {
            return ApiResponse.error("参数不完整");
        }
        
        boolean result = securityQuestionService.setSecurityQuestion(sno, questionId, answer);
        return result ? ApiResponse.success(true) : ApiResponse.error("设置失败");
    }

    /**
     * 获取用户的安全问题
     */
    @GetMapping("/user-questions")
    public ApiResponse<List<Map<String, Object>>> getUserQuestions(@RequestParam String sno) {
        return ApiResponse.success(securityQuestionService.getUserQuestions(sno));
    }

    /**
     * 验证安全问题答案
     */
    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyAnswer(@RequestBody Map<String, Object> params) {
        String sno = (String) params.get("sno");
        Integer questionId = (Integer) params.get("questionId");
        String answer = (String) params.get("answer");
        
        if (sno == null || questionId == null || answer == null) {
            return ApiResponse.error("参数不完整");
        }
        
        boolean result = securityQuestionService.verifyAnswer(sno, questionId, answer);
        return result ? ApiResponse.success(true) : ApiResponse.error("验证失败");
    }

    /**
     * 检查是否已设置安全问题
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> hasSetQuestion(@RequestParam String sno) {
        return ApiResponse.success(securityQuestionService.hasSetQuestion(sno));
    }

    /**
     * 管理员重置用户安全问题
     */
    @PostMapping("/admin/reset")
    public ApiResponse<Boolean> resetQuestion(@RequestBody Map<String, Object> params) {
        String sno = (String) params.get("sno");
        String adminId = (String) params.get("adminId");
        
        if (sno == null || adminId == null) {
            return ApiResponse.error("参数不完整");
        }
        
        boolean result = securityQuestionService.resetQuestion(sno, adminId);
        return result ? ApiResponse.success(true) : ApiResponse.error("重置失败");
    }
} 