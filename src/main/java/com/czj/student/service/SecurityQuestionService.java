package com.czj.student.service;

import com.czj.student.model.entity.QuestionTemplate;
import java.util.List;
import java.util.Map;

public interface SecurityQuestionService {
    /**
     * 设置安全问题
     */
    boolean setSecurityQuestion(String sno, Integer questionId, String answer);
    
    /**
     * 获取用户的安全问题列表
     */
    List<Map<String, Object>> getUserQuestions(String sno);
    
    /**
     * 验证安全问题答案
     */
    boolean verifyAnswer(String sno, Integer questionId, String answer);
    
    /**
     * 检查是否已设置安全问题
     */
    boolean hasSetQuestion(String sno);
    
    /**
     * 重置安全问题(管理员)
     */
    boolean resetQuestion(String sno, String adminId);
    
    /**
     * 获取所有问题模板
     */
    List<QuestionTemplate> getAllQuestionTemplates();
} 