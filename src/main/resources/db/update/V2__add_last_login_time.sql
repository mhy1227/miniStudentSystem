-- 添加最后登录时间字段
ALTER TABLE student 
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间' AFTER login_error_count; 