// 页面加载完成后初始化
window.onload = function() {
    setDefaultSemester();
    // 初始化分页参数
    window.pageConfig = {
        pageNum: 1,
        pageSize: 10
    };
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
    document.getElementById('querySemester').value = defaultSemester;
}

// 重置选课表单
function resetSelectForm() {
    document.getElementById('studentNo').value = '';
    document.getElementById('studentName').textContent = '';
    document.getElementById('studentSid').value = '';
    document.getElementById('courseNo').value = '';
    document.getElementById('courseName').textContent = '';
    document.getElementById('courseCid').value = '';
}

// 格式化日期
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 显示错误信息
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    errorDiv.style.color = '#dc3545';
    errorDiv.style.padding = '10px';
    errorDiv.style.marginBottom = '10px';
    document.querySelector('body').insertBefore(errorDiv, document.querySelector('.nav-menu').nextSibling);
    setTimeout(() => errorDiv.remove(), 3000);
}

// 显示加载状态
function showLoading(show) {
    const loadingDiv = document.getElementById('loadingDiv') || createLoadingDiv();
    loadingDiv.style.display = show ? 'flex' : 'none';
}

// 创建加载状态div
function createLoadingDiv() {
    const div = document.createElement('div');
    div.id = 'loadingDiv';
    div.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255,255,255,0.8);
        display: none;
        justify-content: center;
        align-items: center;
        z-index: 1000;
    `;
    div.innerHTML = '<div class="loading-spinner"></div>';
    document.body.appendChild(div);
    return div;
}

// 查找学生
function searchStudent() {
    const studentNo = document.getElementById('studentNo').value.trim();
    if (!studentNo) {
        showError('请输入学号');
        return;
    }

    showLoading(true);
    fetch(`/api/students/no/${studentNo}`)
        .then(response => response.json())
        .then(result => {
            showLoading(false);
            if (result.code === 200) {
                document.getElementById('studentName').textContent = result.data.name;
                document.getElementById('studentSid').value = result.data.sid;
            } else {
                showError('未找到学生：' + result.message);
                document.getElementById('studentName').textContent = '';
                document.getElementById('studentSid').value = '';
            }
        })
        .catch(error => {
            showLoading(false);
            showError('查询学生失败：' + error);
        });
}

// 查找课程
function searchCourse() {
    const courseNo = document.getElementById('courseNo').value.trim();
    if (!courseNo) {
        showError('请输入课程编号');
        return;
    }

    showLoading(true);
    fetch(`/api/courses/no/${courseNo}`)
        .then(response => response.json())
        .then(result => {
            showLoading(false);
            if (result.code === 200) {
                document.getElementById('courseName').textContent = result.data.name;
                document.getElementById('courseCid').value = result.data.cid;
            } else {
                showError('未找到课程：' + result.message);
                document.getElementById('courseName').textContent = '';
                document.getElementById('courseCid').value = '';
            }
        })
        .catch(error => {
            showLoading(false);
            showError('查询课程失败：' + error);
        });
}

// 选课
function selectCourse() {
    const studentSid = document.getElementById('studentSid').value;
    const courseCid = document.getElementById('courseCid').value;
    const semester = document.getElementById('semester').value.trim();

    if (!studentSid || !courseCid || !semester) {
        showError('请先完成学生和课程的选择，并确保学期已填写');
        return;
    }

    showLoading(true);
    fetch('/api/student-courses/select', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `studentSid=${studentSid}&courseCid=${courseCid}&semester=${semester}`
    })
    .then(response => response.json())
    .then(result => {
        showLoading(false);
        if (result.code === 200) {
            alert('选课成功');
            resetSelectForm();
            searchStudentCourses();
        } else {
            showError('选课失败：' + result.message);
        }
    })
    .catch(error => {
        showLoading(false);
        showError('选课失败：' + error);
    });
}

// 退课
function dropCourse(studentSid, courseCid, semester) {
    if (!confirm('确定要退选这门课程吗？')) {
        return;
    }

    showLoading(true);
    fetch('/api/student-courses/drop', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `studentSid=${studentSid}&courseCid=${courseCid}&semester=${semester}`
    })
    .then(response => response.json())
    .then(result => {
        showLoading(false);
        if (result.code === 200) {
            alert('退课成功');
            searchStudentCourses();
        } else {
            showError('退课失败：' + result.message);
        }
    })
    .catch(error => {
        showLoading(false);
        showError('退课失败：' + error);
    });
}

// 查询选课列表
function searchStudentCourses() {
    const studentSid = document.getElementById('studentSid').value;
    const semester = document.getElementById('querySemester').value.trim();

    if (!studentSid) {
        showError('请先选择学生');
        return;
    }

    showLoading(true);
    fetch(`/api/student-courses/student/${studentSid}?semester=${semester}`)
        .then(response => response.json())
        .then(result => {
            showLoading(false);
            if (result.code === 200) {
                const tbody = document.getElementById('studentCourseList');
                tbody.innerHTML = '';
                
                if (result.data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="7" class="text-center">暂无选课记录</td></tr>';
                    return;
                }

                result.data.forEach(course => {
                    tbody.innerHTML += `
                        <tr>
                            <td>${course.semester}</td>
                            <td>${course.courseNo}</td>
                            <td>${course.courseName}</td>
                            <td>${course.credit}</td>
                            <td>${formatDate(course.selectionDate)}</td>
                            <td>${getStatusText(course.status)}</td>
                            <td>
                                ${course.status === 1 ? 
                                    `<button class="btn-danger" onclick="dropCourse(${course.studentSid}, ${course.courseCid}, '${course.semester}')">退课</button>` : 
                                    ''}
                            </td>
                        </tr>
                    `;
                });
            } else {
                showError('查询选课列表失败：' + result.message);
            }
        })
        .catch(error => {
            showLoading(false);
            console.error('查询选课列表失败:', error);
            document.getElementById('studentCourseList').innerHTML = 
                '<tr><td colspan="7" class="text-center">查询失败，请稍后重试</td></tr>';
        });
}

// 获取状态文本
function getStatusText(status) {
    switch (status) {
        case 1: return '已选课';
        case 2: return '已录入平时成绩';
        case 3: return '已录入考试成绩';
        case 4: return '已完成';
        default: return '未知状态';
    }
}

// 添加CSS样式
const style = document.createElement('style');
style.textContent = `
    .loading-spinner {
        width: 50px;
        height: 50px;
        border: 5px solid #f3f3f3;
        border-top: 5px solid #3498db;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
    
    .error-message {
        animation: fadeOut 3s forwards;
    }
    
    @keyframes fadeOut {
        0% { opacity: 1; }
        70% { opacity: 1; }
        100% { opacity: 0; }
    }
`;
document.head.appendChild(style); 