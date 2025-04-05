-- 添加登录相关字段
ALTER TABLE student 
ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT '123456' COMMENT '登录密码' AFTER sfzh,
ADD COLUMN login_error_count INT DEFAULT 0 COMMENT '登录错误次数' AFTER password; 