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