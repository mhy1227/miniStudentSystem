-- 创建数据库
CREATE DATABASE IF NOT EXISTS mini_student_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE mini_student_db;

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    sid BIGINT PRIMARY KEY AUTO_INCREMENT,
    sno VARCHAR(10) NOT NULL COMMENT '学号（XH前缀+6位数字）',
    name VARCHAR(30) NOT NULL COMMENT '姓名',
    sfzh CHAR(18) NOT NULL COMMENT '身份证号',
    gender CHAR(1) NOT NULL COMMENT '性别：M-男，F-女',
    major VARCHAR(30) NOT NULL COMMENT '专业',
    remark VARCHAR(500) COMMENT '其他说明',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sno (sno),
    UNIQUE KEY uk_sfzh (sfzh),
    KEY idx_name (name)
) COMMENT '学生信息表';

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    cid BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_no VARCHAR(10) NOT NULL COMMENT '课程编号',
    name VARCHAR(50) NOT NULL COMMENT '课程名称',
    credit DECIMAL(2,1) NOT NULL COMMENT '学分',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_course_no (course_no),
    CONSTRAINT chk_credit CHECK (credit >= 0 AND credit <= 10)
) COMMENT '课程信息表';

-- 选课及成绩表
CREATE TABLE IF NOT EXISTS student_course (
    student_sid BIGINT NOT NULL COMMENT '学生ID',
    course_cid BIGINT NOT NULL COMMENT '课程ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已选课，2-已录入平时成绩，3-已录入考试成绩，4-已完成',
    selection_date DATE NOT NULL COMMENT '选课日期',
    regular_score DECIMAL(5,2) DEFAULT NULL COMMENT '平时成绩',
    exam_score DECIMAL(5,2) DEFAULT NULL COMMENT '考试成绩',
    final_score DECIMAL(5,2) DEFAULT NULL COMMENT '最终成绩',
    regular_score_date DATE DEFAULT NULL COMMENT '平时成绩录入日期',
    exam_score_date DATE DEFAULT NULL COMMENT '考试成绩录入日期',
    final_score_date DATE DEFAULT NULL COMMENT '最终成绩录入日期',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (student_sid, course_cid, semester),
    FOREIGN KEY (student_sid) REFERENCES student(sid),
    FOREIGN KEY (course_cid) REFERENCES course(cid),
    KEY idx_semester (semester),
    KEY idx_status (status),
    KEY idx_selection_date (selection_date),
    KEY idx_final_score (final_score),
    CONSTRAINT chk_status CHECK (status IN (1,2,3,4)),
    CONSTRAINT chk_regular_score CHECK (regular_score IS NULL OR (regular_score >= 0 AND regular_score <= 100)),
    CONSTRAINT chk_exam_score CHECK (exam_score IS NULL OR (exam_score >= 0 AND exam_score <= 100)),
    CONSTRAINT chk_final_score CHECK (final_score IS NULL OR (final_score >= 0 AND final_score <= 100))
) COMMENT '选课及成绩信息表';

-- 创建索引
CREATE INDEX idx_student_course_status ON student_course(status);
CREATE INDEX idx_student_course_selection_date ON student_course(selection_date);
CREATE INDEX idx_student_course_final_score ON student_course(final_score);

-- 添加测试数据
INSERT INTO student (sno, name, sfzh, gender, major, remark) VALUES
('XH000001', '张三', '320123200001015678', 'M', '计算机科学与技术', '转专业学生'),
('XH000002', '李四', '320123200002025678', 'M', '软件工程', NULL),
('XH000003', '王五', '320123200003035678', 'F', '人工智能', '特长生'),
('XH000004', '赵六', '320123200004045678', 'M', '计算机科学与技术', NULL),
('XH000005', '钱七', '320123200005055678', 'F', '软件工程', '学生会干部'),
('XH000006', '孙八', '320123200006065678', 'M', '人工智能', NULL),
('XH000007', '周九', '320123200007075678', 'F', '计算机科学与技术', '班长'),
('XH000008', '吴十', '320123200008085678', 'M', '软件工程', NULL),
('XH000009', '郑十一', '320123200009095678', 'F', '人工智能', '竞赛获奖'),
('XH000010', '王十二', '320123200010105678', 'M', '计算机科学与技术', NULL);

INSERT INTO course (course_no, name, credit) VALUES
('C001', '高等数学', 4.0),
('C002', '大学英语', 3.0),
('C003', 'Java程序设计', 3.5),
('C004', '数据结构', 4.0),
('C005', '操作系统', 3.5),
('C006', '计算机网络', 3.0),
('C007', '数据库原理', 3.5),
('C008', '软件工程', 3.0);

-- 添加选课和成绩数据
INSERT INTO student_course 
(student_sid, course_cid, semester, status, selection_date, 
 regular_score, exam_score, final_score, 
 regular_score_date, exam_score_date, final_score_date) 
VALUES
-- 2023-2024-1学期完整成绩数据
(1, 1, '2023-2024-1', 4, '2023-09-01', 85.5, 90.0, 88.5, '2023-12-20', '2024-01-10', '2024-01-15'),
(1, 2, '2023-2024-1', 4, '2023-09-01', 88.0, 85.5, 86.5, '2023-12-20', '2024-01-11', '2024-01-16'),
(2, 1, '2023-2024-1', 4, '2023-09-01', 78.5, 82.0, 80.5, '2023-12-20', '2024-01-10', '2024-01-15'),
(2, 3, '2023-2024-1', 4, '2023-09-01', 92.0, 88.5, 90.0, '2023-12-21', '2024-01-12', '2024-01-15'),
(3, 2, '2023-2024-1', 4, '2023-09-01', 85.0, 88.0, 87.0, '2023-12-20', '2024-01-11', '2024-01-16'),
(3, 4, '2023-2024-1', 4, '2023-09-02', 90.5, 92.0, 91.5, '2023-12-22', '2024-01-13', '2024-01-16'),

-- 2023-2024-1学期部分成绩数据（状态：3-已录入考试成绩）
(4, 1, '2023-2024-1', 3, '2023-09-01', 82.0, 85.0, NULL, '2023-12-20', '2024-01-10', NULL),
(4, 5, '2023-2024-1', 3, '2023-09-02', 88.5, 86.0, NULL, '2023-12-22', '2024-01-13', NULL),

-- 2023-2024-1学期部分成绩数据（状态：2-已录入平时成绩）
(5, 3, '2023-2024-1', 2, '2023-09-01', 85.0, NULL, NULL, '2023-12-21', NULL, NULL),
(5, 6, '2023-2024-1', 2, '2023-09-02', 87.5, NULL, NULL, '2023-12-22', NULL, NULL),

-- 2023-2024-1学期选课数据（状态：1-已选课）
(6, 4, '2023-2024-1', 1, '2023-09-02', NULL, NULL, NULL, NULL, NULL, NULL),
(6, 7, '2023-2024-1', 1, '2023-09-02', NULL, NULL, NULL, NULL, NULL, NULL),

-- 2023-2024-2学期选课数据
(1, 3, '2023-2024-2', 1, '2024-02-20', NULL, NULL, NULL, NULL, NULL, NULL),
(1, 4, '2023-2024-2', 1, '2024-02-20', NULL, NULL, NULL, NULL, NULL, NULL),
(2, 5, '2023-2024-2', 1, '2024-02-20', NULL, NULL, NULL, NULL, NULL, NULL),
(2, 6, '2023-2024-2', 1, '2024-02-20', NULL, NULL, NULL, NULL, NULL, NULL),
(3, 7, '2023-2024-2', 1, '2024-02-21', NULL, NULL, NULL, NULL, NULL, NULL),
(3, 8, '2023-2024-2', 1, '2024-02-21', NULL, NULL, NULL, NULL, NULL, NULL),
(4, 3, '2023-2024-2', 1, '2024-02-21', NULL, NULL, NULL, NULL, NULL, NULL),
(4, 8, '2023-2024-2', 1, '2024-02-21', NULL, NULL, NULL, NULL, NULL, NULL);
