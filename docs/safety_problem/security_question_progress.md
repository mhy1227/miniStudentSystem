# 安全问题验证机制实施进度与设计方案

## 一、当前进度

**项目名称**：安全问题验证机制  
**当前状态**：开发阶段  
**完成度**：45%  
**开始日期**：2025年4月10日  
**预计完成日期**：2025年5月10日  

### 里程碑进度

| 阶段 | 状态 | 开始日期 | 完成/预计日期 | 负责人 |
|------|------|----------|--------------|--------|
| 需求分析 | ✅ 已完成 | 2025-04-08 | 2025-04-10 | 系统管理员 |
| 设计方案 | ✅ 已完成 | 2025-04-10 | 2025-04-12 | 系统管理员 |
| 数据库设计 | ✅ 已完成 | 2025-04-10 | 2025-04-12 | 系统管理员 |
| 后端开发 | 🔄 进行中 | 2025-04-12 | 2025-04-25 | 系统管理员 |
| 前端开发 | ⏳ 未开始 | 2025-04-20 | 2025-05-05 | 待定 |
| 测试与上线 | ⏳ 未开始 | 2025-05-05 | 2025-05-10 | 待定 |

### 已完成工作

- [x] 需求分析文档
- [x] 用户场景梳理
- [x] 异地登录与安全问题验证流程讨论
- [x] 安全问题应用场景确定：异地登录检测和密码重置
- [x] 数据库表设计完成
- [x] API接口设计完成
- [x] 核心服务接口实现
- [x] 与登录系统集成完成

### 进行中工作

- [ ] 完善错误处理机制
- [ ] 添加日志记录
- [ ] 实现前端页面
- [ ] 编写单元测试

## 二、简化设计方案

### 1. 总体架构

安全问题验证机制将整合到现有的用户认证系统中，主要应用于两个场景：
1. 异地登录检测时的身份验证
2. 密码重置/找回流程

采用简化的实现策略：
- 每个用户只需设置一个安全问题和答案
- 使用现有用户表进行扩展，避免复杂的表关联
- 先实现基础功能，后续再优化扩展

### 2. 数据库设计（简化版）

选择直接在学生表（student）中添加安全问题相关字段，简化表结构和查询逻辑。

**学生表扩展字段：**
```sql
ALTER TABLE `student` 
ADD COLUMN `security_question` VARCHAR(255) NULL COMMENT '安全问题',
ADD COLUMN `security_answer_hash` VARCHAR(128) NULL COMMENT '安全问题答案(哈希)',
ADD COLUMN `security_answer_salt` VARCHAR(32) NULL COMMENT '安全问题答案盐值'; 
```

这种设计的优点：
- 实现简单，无需创建额外的表
- 查询高效，直接通过学号获取安全问题和答案
- 便于与现有功能集成

缺点（后期可优化）：
- 每个用户只能设置一个安全问题
- 无法记录验证尝试历史

### 3. 核心类设计（简化版）

#### 安全问题服务接口
```java
public interface SecurityQuestionService {
    // 设置用户安全问题
    boolean setSecurityQuestion(String sno, String question, String answer);
    
    // 获取用户的安全问题
    String getSecurityQuestion(String sno);
    
    // 验证安全问题答案
    boolean verifySecurityAnswer(String sno, String answer);
    
    // 检查用户是否已设置安全问题
    boolean hasSetSecurityQuestion(String sno);
}
```

#### 安全问题服务实现
```java
@Service
public class SecurityQuestionServiceImpl implements SecurityQuestionService {
    @Resource
    private StudentMapper studentMapper;
    
    // 设置安全问题
    @Override
    public boolean setSecurityQuestion(String sno, String question, String answer) {
        // 生成随机盐值
        String salt = generateSalt();
        // 哈希处理答案
        String hashedAnswer = hashWithSalt(answer, salt);
        
        // 更新学生表
        return studentMapper.updateSecurityQuestion(sno, question, hashedAnswer, salt) > 0;
    }
    
    // 获取安全问题
    @Override
    public String getSecurityQuestion(String sno) {
        Student student = studentMapper.selectByStudentNo(sno);
        return student != null ? student.getSecurityQuestion() : null;
    }
    
    // 验证答案
    @Override
    public boolean verifySecurityAnswer(String sno, String answer) {
        Student student = studentMapper.selectByStudentNo(sno);
        if (student == null || student.getSecurityAnswerHash() == null) {
            return false;
        }
        
        String hashedInput = hashWithSalt(answer, student.getSecurityAnswerSalt());
        return hashedInput.equals(student.getSecurityAnswerHash());
    }
    
    // 检查是否设置
    @Override
    public boolean hasSetSecurityQuestion(String sno) {
        Student student = studentMapper.selectByStudentNo(sno);
        return student != null && student.getSecurityQuestion() != null;
    }
    
    // 生成盐值
    private String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    // 哈希函数
    private String hashWithSalt(String answer, String salt) {
        String normalizedAnswer = answer.trim().toLowerCase();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(normalizedAnswer.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("哈希算法不可用", e);
        }
    }
}
```

### 4. API接口设计（简化版）

#### 安全问题设置

```
1. 设置安全问题
POST /api/security-question/set
Request:
{
  "question": "您的出生地是？",
  "answer": "北京"
}
Response:
{
  "code": 200,
  "message": "安全问题设置成功",
  "data": true
}

2. 获取安全问题
GET /api/security-question
Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "hasSet": true,
    "question": "您的出生地是？"
  }
}
```

#### 异地登录验证

```
1. 获取安全问题（异地登录时）
POST /api/auth/remote-login/question
Request:
{
  "sno": "XH202501",
  "password": "encoded_password"
}
Response:
{
  "code": 202,
  "message": "需要安全验证",
  "data": {
    "question": "您的出生地是？",
    "verifyToken": "xyz123"
  }
}

2. 验证安全问题（异地登录时）
POST /api/auth/remote-login/verify
Request:
{
  "sno": "XH202501",
  "answer": "北京",
  "verifyToken": "xyz123"
}
Response:
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "token": "jwt_token",
    "userInfo": {
      // 用户信息
    }
  }
}
```

#### 密码重置

```
1. 发起密码重置
POST /api/password/reset-request
Request:
{
  "sno": "XH202501"
}
Response:
{
  "code": 200,
  "message": "请回答安全问题",
  "data": {
    "question": "您的出生地是？",
    "resetToken": "abc456"
  }
}

2. 验证并重置密码
POST /api/password/reset
Request:
{
  "sno": "XH202501",
  "answer": "北京",
  "newPassword": "new_password",
  "resetToken": "abc456"
}
Response:
{
  "code": 200,
  "message": "密码重置成功",
  "data": true
}
```

### 5. 前端界面设计（简化版）

#### 安全问题设置界面
- 位置：用户个人中心页面 → 安全设置
- 组件：
  - 安全问题输入框（支持用户自定义问题）
  - 安全问题答案输入框
  - 保存按钮

#### 异地登录验证界面
- 位置：登录过程中的弹窗
- 组件：
  - 安全提示文本
  - 安全问题显示
  - 答案输入框
  - 验证按钮
  - 取消按钮

#### 密码重置界面
- 位置：登录页面的"忘记密码"入口
- 步骤：
  1. 输入学号页面
  2. 安全问题验证页面
  3. 设置新密码页面

## 三、实施计划（简化版）

### 1. 第一阶段：基础设施（1周）
- 数据库字段添加
- 安全问题服务实现
- 安全问题设置功能

### 2. 第二阶段：异地登录验证（1周）
- 登录控制器修改
- 异地登录验证流程
- 前端验证界面

### 3. 第三阶段：密码重置功能（1周）
- 密码重置流程实现
- 前端界面开发
- 测试与上线

## 四、注意事项

1. **安全性保证**：
   - 即使简化设计，也要确保答案安全存储（哈希+盐值）
   - 限制验证失败次数，防止暴力破解

2. **用户体验**：
   - 提供默认的安全问题示例，帮助用户选择合适的问题
   - 提示用户选择容易记住但他人难以猜测的答案

3. **向后兼容**：
   - 在用户首次登录时提示设置安全问题
   - 对于未设置安全问题的用户，暂时使用原有的登录机制

## 五、后续优化方向

待基础功能稳定运行后，可考虑以下优化：

1. **多安全问题支持**：
   - 创建独立的安全问题表
   - 支持用户设置多个问题，随机抽取验证

2. **验证记录追踪**：
   - 添加验证尝试记录表
   - 实现更精细的安全控制策略

3. **智能风险评估**：
   - 根据IP变化程度、时间因素评估风险级别
   - 对低风险登录减少验证频率 