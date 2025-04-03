-- 创建数据库
CREATE DATABASE IF NOT EXISTS mini_student_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE mini_student_db;

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    sfzh VARCHAR(18) NOT NULL UNIQUE COMMENT '身份证号',
    gender CHAR(1) NOT NULL COMMENT '性别：M-男，F-女',
    major VARCHAR(50) NOT NULL COMMENT '专业',
    remark VARCHAR(500) COMMENT '其他说明',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '学生信息表';

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_no VARCHAR(20) NOT NULL UNIQUE COMMENT '课程编号',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit DECIMAL(2,1) NOT NULL COMMENT '学分',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '课程信息表';

-- 选课表
CREATE TABLE IF NOT EXISTS course_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_student_course (student_id, course_id, semester),
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_id) REFERENCES course(id)
) COMMENT '选课信息表';

-- 成绩表
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    grade_no VARCHAR(32) NOT NULL UNIQUE COMMENT '成绩编号',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    regular_score DECIMAL(5,2) COMMENT '平时成绩',
    exam_score DECIMAL(5,2) COMMENT '考核成绩',
    final_score DECIMAL(5,2) COMMENT '最终成绩',
    grade_date DATE NOT NULL COMMENT '添加日期',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester),
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (course_id) REFERENCES course(id)
) COMMENT '成绩信息表';

-- 添加一些测试数据
INSERT INTO student (student_no, name, sfzh, gender, major, remark) VALUES
('2021001', '张三', '320123200001015678', 'M', '计算机科学与技术', '转专业学生'),
('2021002', '李四', '320123200002025678', 'M', '软件工程', NULL),
('2021003', '王五', '320123200003035678', 'F', '人工智能', '特长生');

INSERT INTO course (course_no, name, credit) VALUES
('C001', '高等数学', 4.0),
('C002', '大学英语', 3.0),
('C003', 'Java程序设计', 3.5);

-- 添加选课数据
INSERT INTO course_selection (student_id, course_id, semester) VALUES
(1, 1, '2023-2024-1'),
(1, 2, '2023-2024-1'),
(2, 1, '2023-2024-1');

-- 添加成绩数据
INSERT INTO grade (grade_no, student_id, course_id, course_name, semester, regular_score, exam_score, final_score, grade_date) VALUES
('GR202401001', 1, 1, '高等数学', '2023-2024-1', 85.5, 90.0, 88.5, '2024-01-15'),
('GR202401002', 1, 2, '大学英语', '2023-2024-1', 88.0, 85.5, 86.5, '2024-01-16'),
('GR202401003', 2, 1, '高等数学', '2023-2024-1', 78.5, 82.0, 80.5, '2024-01-15');
