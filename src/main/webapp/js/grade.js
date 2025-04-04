// 全局变量
let currentStudent = null;
let currentCourse = null;

// 显示加载状态
function showLoading() {
    const loadingDiv = document.getElementById('loadingIndicator');
    if (loadingDiv) {
        loadingDiv.style.display = 'block';
    }
}

// 隐藏加载状态
function hideLoading() {
    const loadingDiv = document.getElementById('loadingIndicator');
    if (loadingDiv) {
        loadingDiv.style.display = 'none';
    }
}

// 页面加载完成后设置默认学期
window.onload = function() {
    setDefaultSemester();
};

// 设置默认学期
function setDefaultSemester() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const semester = month >= 8 ? '1' : '2';
    const academicYear = month >= 8 ? `${year}-${year+1}` : `${year-1}-${year}`;
    const defaultSemester = `${academicYear}-${semester}`;
    
    document.getElementById('semester').value = defaultSemester;
}

// 查询学生信息
async function searchStudent() {
    const studentId = document.getElementById('studentId').value.trim();
    if (!studentId) {
        alert('请输入学号');
        return;
    }

    showLoading();
    try {
        const response = await fetch(`/api/students/no/${studentId}`);
        const result = await response.json();
        
        if (result.code === 200 && result.data) {
            currentStudent = result.data;
            document.getElementById('studentName').textContent = currentStudent.name;
            loadGrades();
        } else {
            alert('未找到该学生');
            document.getElementById('studentName').textContent = '';
            currentStudent = null;
        }
    } catch (error) {
        console.error('查询学生失败:', error);
        alert('查询学生失败');
    } finally {
        hideLoading();
    }
}

// 查找课程
async function searchCourse() {
    const courseId = document.getElementById("courseId").value;
    if (!courseId) {
        alert("请输入课程号");
        return;
    }

    showLoading();
    try {
        const response = await fetch(`/api/courses/no/${courseId}`);
        const result = await response.json();
        
        if (result.code === 200 && result.data) {
            currentCourse = result.data;
            document.getElementById("courseName").textContent = currentCourse.name;
            loadCourseGrades();
        } else {
            alert("未找到该课程");
            document.getElementById("courseName").textContent = "";
            currentCourse = null;
        }
    } catch (error) {
        console.error("查询课程失败:", error);
        alert("查询课程失败");
    } finally {
        hideLoading();
    }
}

// 录入平时成绩
async function updateRegularScore() {
    if (!validateScoreInput()) return;
    
    const regularScore = document.getElementById("regularScore").value;
    const params = new URLSearchParams({
        studentSid: currentStudent.sid,
        courseCid: currentCourse.cid,
        semester: document.getElementById("semester").value,
        regularScore: regularScore
    });

    showLoading();
    try {
        const response = await fetch(`/api/student-courses/regular-score?${params}`, {
            method: 'POST'
        });
        const result = await response.json();
        
        if (result.code === 200) {
            alert("平时成绩录入成功");
            loadGrades();
        } else {
            alert(result.message || "平时成绩录入失败");
        }
    } catch (error) {
        console.error("录入平时成绩失败:", error);
        alert("录入平时成绩失败");
    } finally {
        hideLoading();
    }
}

// 录入考试成绩
async function updateExamScore() {
    if (!validateScoreInput()) return;
    
    const examScore = document.getElementById("examScore").value;
    const params = new URLSearchParams({
        studentSid: currentStudent.sid,
        courseCid: currentCourse.cid,
        semester: document.getElementById("semester").value,
        examScore: examScore
    });

    showLoading();
    try {
        const response = await fetch(`/api/student-courses/exam-score?${params}`, {
            method: 'POST'
        });
        const result = await response.json();
        
        if (result.code === 200) {
            alert("考试成绩录入成功");
            loadGrades();
        } else {
            alert(result.message || "考试成绩录入失败");
        }
    } catch (error) {
        console.error("录入考试成绩失败:", error);
        alert("录入考试成绩失败");
    } finally {
        hideLoading();
    }
}

// 加载学生成绩
async function loadStudentGrades() {
    if (!currentStudent) return;
    
    const semester = document.getElementById("semester").value;
    showLoading();
    try {
        const response = await fetch(`/api/student-courses/grades/student/${currentStudent.sid}?semester=${semester}`);
        const result = await response.json();
        
        if (result.code === 200) {
            displayGrades(result.data);
            document.getElementById("gradeStats").style.display = "none";
        } else {
            alert(result.message || "查询成绩失败");
        }
    } catch (error) {
        console.error("加载学生成绩失败:", error);
        alert("加载学生成绩失败");
    } finally {
        hideLoading();
    }
}

// 加载课程成绩
async function loadCourseGrades() {
    if (!currentCourse) return;
    
    const semester = document.getElementById("semester").value;
    showLoading();
    try {
        const response = await fetch(`/api/student-courses/grades/course/${currentCourse.cid}?semester=${semester}`);
        const result = await response.json();
        
        if (result.code === 200) {
            displayGrades(result.data);
            loadCourseStats();
        } else {
            alert(result.message || "查询成绩失败");
        }
    } catch (error) {
        console.error("加载课程成绩失败:", error);
        alert("加载课程成绩失败");
    } finally {
        hideLoading();
    }
}

// 加载课程统计信息
async function loadCourseStats() {
    if (!currentCourse) return;
    
    const semester = document.getElementById("semester").value;
    try {
        const response = await fetch(`/api/student-courses/grades/stats/${currentCourse.cid}?semester=${semester}`);
        const result = await response.json();
        
        if (result.code === 200) {
            displayStats(result.data);
        } else {
            console.error("加载统计信息失败:", result.message);
        }
    } catch (error) {
        console.error("加载统计信息失败:", error);
    }
}

// 显示成绩列表
function displayGrades(grades) {
    const tbody = document.getElementById("gradeList");
    tbody.innerHTML = "";
    
    if (!grades || grades.length === 0) {
        const tr = document.createElement("tr");
        tr.innerHTML = '<td colspan="9" class="text-center">暂无成绩数据</td>';
        tbody.appendChild(tr);
        return;
    }
    
    grades.forEach(grade => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${grade.studentNo || '-'}</td>
            <td>${grade.studentName || '-'}</td>
            <td>${grade.courseNo || '-'}</td>
            <td>${grade.courseName || '-'}</td>
            <td>${grade.semester || '-'}</td>
            <td>${grade.regularScore !== null ? grade.regularScore.toFixed(1) : '-'}</td>
            <td>${grade.examScore !== null ? grade.examScore.toFixed(1) : '-'}</td>
            <td>${grade.finalScore !== null ? grade.finalScore.toFixed(1) : '-'}</td>
            <td>${getStatusText(grade.status)}</td>
        `;
        tbody.appendChild(tr);
    });
}

// 显示成绩统计信息
function displayStats(stats) {
    const statsContainer = document.getElementById("gradeStats");
    if (!stats) {
        statsContainer.style.display = "none";
        return;
    }

    document.getElementById("avgScore").textContent = stats.averageScore ? stats.averageScore.toFixed(1) : "-";
    document.getElementById("maxScore").textContent = stats.maxScore ? stats.maxScore.toFixed(1) : "-";
    document.getElementById("minScore").textContent = stats.minScore ? stats.minScore.toFixed(1) : "-";
    document.getElementById("passRate").textContent = stats.passRate ? (stats.passRate * 100).toFixed(1) + "%" : "-";
    
    statsContainer.style.display = "block";
}

// 获取状态文本
function getStatusText(status) {
    switch (status) {
        case 1: return "已选课";
        case 2: return "已录入平时成绩";
        case 3: return "已录入考试成绩";
        case 4: return "已完成";
        default: return "未知状态";
    }
}

// 验证成绩输入
function validateScoreInput() {
    if (!currentStudent) {
        alert("请先选择学生");
        return false;
    }
    if (!currentCourse) {
        alert("请先选择课程");
        return false;
    }
    
    const semester = document.getElementById("semester").value;
    if (!semester) {
        alert("请输入学期");
        return false;
    }
    
    const regularScoreInput = document.getElementById("regularScore");
    const examScoreInput = document.getElementById("examScore");
    const score = regularScoreInput.value || examScoreInput.value;
    
    if (!score) {
        alert("请输入成绩");
        return false;
    }
    
    const scoreNum = parseFloat(score);
    if (isNaN(scoreNum) || scoreNum < 0 || scoreNum > 100) {
        alert("成绩必须在0-100之间");
        return false;
    }
    
    return true;
}

// 加载成绩
function loadGrades() {
    if (currentStudent) {
        loadStudentGrades();
    } else if (currentCourse) {
        loadCourseGrades();
    }
} 