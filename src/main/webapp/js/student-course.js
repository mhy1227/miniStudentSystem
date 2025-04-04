// 查找学生
function searchStudent() {
    const studentNo = document.getElementById('studentNo').value;
    if (!studentNo) {
        alert('请输入学号');
        return;
    }

    fetch(`/api/students/no/${studentNo}`)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                const student = result.data;
                document.getElementById('studentSid').value = student.sid;
                document.getElementById('studentName').textContent = student.name;
            } else {
                alert('查找学生失败：' + result.message);
            }
        })
        .catch(error => alert('查找学生失败：' + error));
}

// 查找课程
function searchCourse() {
    const courseNo = document.getElementById('courseNo').value;
    if (!courseNo) {
        alert('请输入课程编号');
        return;
    }

    fetch(`/api/courses/no/${courseNo}`)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                const course = result.data;
                document.getElementById('courseCid').value = course.cid;
                document.getElementById('courseName').textContent = course.name;
            } else {
                alert('查找课程失败：' + result.message);
            }
        })
        .catch(error => alert('查找课程失败：' + error));
}

// 选课
function selectCourse() {
    const studentSid = document.getElementById('studentSid').value;
    const courseCid = document.getElementById('courseCid').value;
    const semester = document.getElementById('semester').value;

    if (!studentSid) {
        alert('请先查找学生');
        return;
    }
    if (!courseCid) {
        alert('请先查找课程');
        return;
    }
    if (!semester) {
        alert('请输入学期');
        return;
    }

    const params = new URLSearchParams();
    params.append('studentSid', studentSid);
    params.append('courseCid', courseCid);
    params.append('semester', semester);

    fetch('/api/student-courses/select', {
        method: 'POST',
        body: params
    })
    .then(response => response.json())
    .then(result => {
        if (result.code === 200) {
            alert('选课成功');
            searchStudentCourses();
        } else {
            alert('选课失败：' + result.message);
        }
    })
    .catch(error => alert('选课失败：' + error));
}

// 退课
function dropCourse(studentSid, courseCid, semester) {
    if (!confirm('确定要退选这门课程吗？')) {
        return;
    }

    const params = new URLSearchParams();
    params.append('studentSid', studentSid);
    params.append('courseCid', courseCid);
    params.append('semester', semester);

    fetch('/api/student-courses/drop', {
        method: 'POST',
        body: params
    })
    .then(response => response.json())
    .then(result => {
        if (result.code === 200) {
            alert('退课成功');
            searchStudentCourses();
        } else {
            alert('退课失败：' + result.message);
        }
    })
    .catch(error => alert('退课失败：' + error));
}

// 查询学生的选课列表
function searchStudentCourses() {
    const studentSid = document.getElementById('studentSid').value;
    const semester = document.getElementById('querySemester').value;

    if (!studentSid) {
        alert('请先查找学生');
        return;
    }

    let url = `/api/student-courses/student/${studentSid}`;
    if (semester) {
        url += `?semester=${semester}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                const tbody = document.getElementById('studentCourseList');
                tbody.innerHTML = '';
                result.data.forEach(course => {
                    tbody.innerHTML += `
                        <tr>
                            <td>${course.semester}</td>
                            <td>${course.courseNo}</td>
                            <td>${course.courseName}</td>
                            <td>${course.credit}</td>
                            <td>${new Date(course.selectionDate).toLocaleDateString()}</td>
                            <td>${getStatusText(course.status)}</td>
                            <td>
                                ${course.status === 1 ? 
                                    `<button onclick="dropCourse(${course.studentSid}, ${course.courseCid}, '${course.semester}')">退课</button>` 
                                    : ''}
                            </td>
                        </tr>
                    `;
                });
            } else {
                alert('查询选课列表失败：' + result.message);
            }
        })
        .catch(error => alert('查询选课列表失败：' + error));
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