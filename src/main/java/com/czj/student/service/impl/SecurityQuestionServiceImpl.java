package com.czj.student.service.impl;

import com.czj.student.mapper.SecurityQuestionMapper;
import com.czj.student.mapper.QuestionTemplateMapper;
import com.czj.student.model.entity.SecurityQuestion;
import com.czj.student.model.entity.QuestionTemplate;
import com.czj.student.service.SecurityQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
public class SecurityQuestionServiceImpl implements SecurityQuestionService {
    @Resource
    private SecurityQuestionMapper securityQuestionMapper;
    
    @Resource
    private QuestionTemplateMapper questionTemplateMapper;
    
    // 允许的最大连续失败次数
    private static final int MAX_FAIL_COUNT = 5;
    // 锁定时间(分钟)
    private static final int LOCK_MINUTES = 30;
    
    @Override
    public boolean setSecurityQuestion(String sno, Integer questionId, String answer) {
        // 验证问题模板是否存在
        QuestionTemplate template = questionTemplateMapper.selectById(questionId);
        if (template == null || template.getStatus() != 1) {
            return false;
        }
        
        // 标准化答案
        String normalizedAnswer = normalizeAnswer(answer);
        
        List<SecurityQuestion> questions = securityQuestionMapper.selectBySno(sno);
        
        // 检查是否已存在相同问题
        for (SecurityQuestion q : questions) {
            if (q.getQuestionId().equals(questionId)) {
                q.setAnswer(normalizedAnswer);
                q.setStatus(1); // 重新激活
                q.setFailCount(0);
                q.setLastFailTime(null);
                return securityQuestionMapper.update(q) > 0;
            }
        }
        
        // 创建新问题
        SecurityQuestion sq = new SecurityQuestion();
        sq.setSno(sno);
        sq.setQuestionId(questionId);
        sq.setAnswer(normalizedAnswer);
        sq.setStatus(1);
        sq.setFailCount(0);
        return securityQuestionMapper.insert(sq) > 0;
    }
    
    @Override
    public List<Map<String, Object>> getUserQuestions(String sno) {
        List<SecurityQuestion> questions = securityQuestionMapper.selectBySno(sno);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (SecurityQuestion sq : questions) {
            if (sq.getStatus() > 0) {  // 只返回正常和已验证的问题
                QuestionTemplate template = questionTemplateMapper.selectById(sq.getQuestionId());
                if (template != null) {
                    Map<String, Object> questionInfo = new HashMap<>();
                    questionInfo.put("id", sq.getId());
                    questionInfo.put("questionId", sq.getQuestionId());
                    questionInfo.put("question", template.getQuestion());
                    questionInfo.put("category", template.getCategory());
                    result.add(questionInfo);
                }
            }
        }
        
        return result;
    }
    
    @Override
    public boolean verifyAnswer(String sno, Integer questionId, String answer) {
        List<SecurityQuestion> questions = securityQuestionMapper.selectBySno(sno);
        
        for (SecurityQuestion sq : questions) {
            if (sq.getQuestionId().equals(questionId)) {
                // 检查是否被锁定
                if (sq.getFailCount() >= MAX_FAIL_COUNT && sq.getLastFailTime() != null) {
                    Date lockUntil = new Date(sq.getLastFailTime().getTime() + LOCK_MINUTES * 60 * 1000);
                    if (new Date().before(lockUntil)) {
                        return false; // 仍在锁定期
                    }
                }
                
                // 标准化答案
                String normalizedAnswer = normalizeAnswer(answer);
                boolean isCorrect = normalizedAnswer.equals(sq.getAnswer());
                
                if (isCorrect) {
                    // 重置失败计数
                    securityQuestionMapper.resetFailCount(sq.getId());
                    // 更新状态为已验证
                    sq.setStatus(2); // 已验证
                    securityQuestionMapper.update(sq);
                    return true;
                } else {
                    // 增加失败计数
                    sq.setFailCount(sq.getFailCount() + 1);
                    sq.setLastFailTime(new Date());
                    securityQuestionMapper.updateFailCount(sq.getId(), sq.getFailCount());
                    return false;
                }
            }
        }
        
        return false; // 问题不存在
    }
    
    @Override
    public boolean hasSetQuestion(String sno) {
        List<SecurityQuestion> questions = securityQuestionMapper.selectBySno(sno);
        return questions != null && !questions.isEmpty();
    }
    
    @Override
    public boolean resetQuestion(String sno, String adminId) {
        // 这里可以添加管理员权限验证
        List<SecurityQuestion> questions = securityQuestionMapper.selectBySno(sno);
        
        for (SecurityQuestion sq : questions) {
            sq.setStatus(0); // 禁用当前问题
            securityQuestionMapper.update(sq);
        }
        
        // 这里可以记录管理员操作日志
        return true;
    }
    
    @Override
    public List<QuestionTemplate> getAllQuestionTemplates() {
        return questionTemplateMapper.selectAll();
    }
    
    // 标准化答案
    private String normalizeAnswer(String answer) {
        if (answer == null) return "";
        // 去除空格，转小写
        return answer.trim().toLowerCase();
    }
} 