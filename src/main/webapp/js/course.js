// 页面加载完成后获取课程列表
window.onload = function() {
    loadCourses();
};

// 加载课程列表
function loadCourses() {
    console.log('开始加载课程列表...');
    fetch('/api/courses', {
        headers: {
            'Accept': 'application/json;charset=UTF-8'
        }
    })
        .then(response => {
            console.log('收到响应:', response);
            return response.json();
        })
        .then(result => {
            console.log('解析后的数据:', result);
            if (result.code === 200) {
                const tbody = document.getElementById('courseList');
                tbody.innerHTML = '';
                result.data.list.forEach(course => {
                    tbody.innerHTML += `
                        <tr>
                            <td>${course.courseNo}</td>
                            <td>${course.name}</td>
                            <td>${course.credit}</td>
                            <td>
                                <button onclick="editCourse(${course.cid})">编辑</button>
                                <button onclick="deleteCourse(${course.cid})">删除</button>
                            </td>
                        </tr>
                    `;
                });
            } else {
                alert('获取课程列表失败：' + result.message);
            }
        })
        .catch(error => {
            console.error('请求失败:', error);
            alert('获取课程列表失败：' + error);
        });
}

// 保存课程信息
function saveCourse() {
    const course = {
        courseNo: document.getElementById('courseNo').value,
        name: document.getElementById('name').value,
        credit: parseFloat(document.getElementById('credit').value)
    };

    const cid = document.getElementById('cid').value;
    const method = cid ? 'PUT' : 'POST';
    const url = cid ? `/api/courses/${cid}` : '/api/courses';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
            'Accept': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify(course)
    })
    .then(response => response.json())
    .then(result => {
        if (result.code === 200) {
            alert('保存成功');
            resetForm();
            loadCourses();
        } else {
            alert('保存失败：' + result.message);
        }
    })
    .catch(error => alert('保存失败：' + error));
}

// 编辑课程
function editCourse(cid) {
    fetch(`/api/courses/${cid}`)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                const course = result.data;
                document.getElementById('cid').value = course.cid;
                document.getElementById('courseNo').value = course.courseNo;
                document.getElementById('name').value = course.name;
                document.getElementById('credit').value = course.credit;
            } else {
                alert('获取课程信息失败：' + result.message);
            }
        })
        .catch(error => alert('获取课程信息失败：' + error));
}

// 删除课程
function deleteCourse(cid) {
    if (confirm('确定要删除这个课程吗？')) {
        fetch(`/api/courses/${cid}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                alert('删除成功');
                loadCourses();
            } else {
                alert('删除失败：' + result.message);
            }
        })
        .catch(error => alert('删除失败：' + error));
    }
}

// 重置表单
function resetForm() {
    document.getElementById('cid').value = '';
    document.getElementById('courseNo').value = '';
    document.getElementById('name').value = '';
    document.getElementById('credit').value = '';
} 