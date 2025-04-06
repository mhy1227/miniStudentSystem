-- 更新现有学生的密码为身份证后6位
UPDATE student 
SET pwd = RIGHT(sfzh, 6); 