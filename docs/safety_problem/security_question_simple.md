# 安全问题验证机制 - 优化方案

## 一、总体概述

**项目名称**：安全问题验证机制  
**当前状态**：设计阶段  
**完成度**：20%  
**预计完成时间**：5天  

## 二、数据库设计

### 安全问题表（security_question）

```sql
CREATE TABLE `security_question` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` INT NOT NULL COMMENT '问题模板ID',
  `answer` VARCHAR(255) NOT NULL COMMENT '安全问题答案',
  `sno` VARCHAR(10) NOT NULL COMMENT '学号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-已验证，0-已禁用',
  `fail_count` INT DEFAULT 0 COMMENT '连续验证失败次数',
  `last_fail_time` DATETIME DEFAULT NULL COMMENT '最后一次验证失败时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_sno` (`sno`),
  INDEX `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全问题表';
```

### 问题模板表（question_template）

```sql
CREATE TABLE `question_template` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question` VARCHAR(255) NOT NULL COMMENT '问题内容',
  `category` VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '问题类别',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全问题模板表';

-- 初始化一些常用问题
INSERT INTO `question_template` (`question`, `category`) VALUES 
('您的出生地是？', 'PERSONAL'),
('您母亲的姓名是？', 'FAMILY'),
('您的小学班主任是？', 'EDUCATION'),
('您最喜欢的颜色是？', 'PREFERENCE'),
('您的第一个宠物名字是？', 'PERSONAL');
```

## 三、核心代码

### 实体类

```java
@Data
public class SecurityQuestion {
    private Integer id;
    private Integer questionId;
    private String answer;
    private String sno;
    private Integer status;
    private Integer failCount;
    private Date lastFailTime;
    private Date createTime;
    private Date updateTime;
    
    // 非表字段
    @Transient
    private String questionContent;
}

@Data
public class QuestionTemplate {
    private Integer id;
    private String question;
    private String category;
    private Integer status;
    private Date createTime;
}
```

### Mapper接口

```java
public interface SecurityQuestionMapper {
    int insert(SecurityQuestion question);
    int update(SecurityQuestion question);
    List<SecurityQuestion> selectBySno(String sno);
    SecurityQuestion selectById(Integer id);
    int updateFailCount(@Param("id") Integer id, @Param("failCount") Integer failCount);
    int resetFailCount(Integer id);
}

public interface QuestionTemplateMapper {
    List<QuestionTemplate> selectAll();
    List<QuestionTemplate> selectByCategory(String category);
    QuestionTemplate selectById(Integer id);
}
```

### 服务接口

```java
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
```

### 服务实现

```java
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
```

## 四、API设计

### 1. 获取问题模板列表
```
GET /api/auth/question-templates

响应：
{
    "code": 200,
    "message": "成功",
    "data": [
        {
            "id": 1,
            "question": "您的出生地是？",
            "category": "PERSONAL"
        },
        ...
    ]
}
```

### 2. 设置安全问题
```
POST /api/auth/security-question
请求体：
{
    "questionId": 1,
    "answer": "北京"
}
响应：
{
    "code": 200,
    "message": "设置成功",
    "data": true
}
```

### 3. 获取用户安全问题
```
GET /api/auth/user-questions

响应：
{
    "code": 200,
    "message": "成功",
    "data": [
        {
            "id": 5,
            "questionId": 1,
            "question": "您的出生地是？",
            "category": "PERSONAL"
        },
        ...
    ]
}
```

### 4. 验证安全问题
```
POST /api/auth/verify-question
请求体：
{
    "questionId": 1,
    "answer": "北京"
}
响应：
{
    "code": 200,
    "message": "验证成功",
    "data": true
}
```

### 5. 管理员重置用户安全问题
```
POST /api/admin/reset-security-question
请求体：
{
    "sno": "XH000001"
}
响应：
{
    "code": 200,
    "message": "重置成功",
    "data": true
}
```

## 五、实施计划

### 第1天：数据库与基础设计
- 创建security_question和question_template表
- 初始化常用问题数据
- 开发实体类和Mapper接口

### 第2-3天：核心功能实现
- 实现安全问题服务
- 实现问题验证与失败处理逻辑
- 开发管理员重置功能

### 第4天：API和前端整合
- 实现控制器接口
- 开发安全问题设置页面
- 整合验证流程到登录系统

### 第5天：测试与上线
- 单元测试
- 集成测试
- 功能上线 