// 页面加载完成后获取学生列表
window.onload = function() {
    loadStudents();
};

// 加载学生列表
function loadStudents() {
    console.log('开始加载学生列表...');
    fetch('/api/students', {
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
                const tbody = document.getElementById('studentList');
                tbody.innerHTML = '';
                result.data.list.forEach(student => {
                    tbody.innerHTML += `
                        <tr>
                            <td>${student.sno}</td>
                            <td>${student.name}</td>
                            <td>${student.gender === 'M' ? '男' : '女'}</td>
                            <td>${student.major}</td>
                            <td>
                                <button onclick="editStudent(${student.sid})">编辑</button>
                                <button onclick="deleteStudent(${student.sid})">删除</button>
                            </td>
                        </tr>
                    `;
                });
            } else {
                alert('获取学生列表失败：' + result.message);
            }
        })
        .catch(error => {
            console.error('请求失败:', error);
            alert('获取学生列表失败：' + error);
        });
}

// 保存学生信息
function saveStudent() {
    const student = {
        sno: document.getElementById('sno').value,
        name: document.getElementById('name').value,
        sfzh: document.getElementById('sfzh').value,
        gender: document.getElementById('gender').value,
        major: document.getElementById('major').value,
        remark: document.getElementById('remark').value
    };

    const sid = document.getElementById('sid').value;
    const method = sid ? 'PUT' : 'POST';
    const url = sid ? `/api/students/${sid}` : '/api/students';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
            'Accept': 'application/json;charset=UTF-8'
        },
        body: JSON.stringify(student)
    })
    .then(response => response.json())
    .then(result => {
        if (result.code === 200) {
            alert('保存成功');
            resetForm();
            loadStudents();
        } else {
            alert('保存失败：' + result.message);
        }
    })
    .catch(error => alert('保存失败：' + error));
}

// 编辑学生
function editStudent(sid) {
    fetch(`/api/students/${sid}`)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                const student = result.data;
                document.getElementById('sid').value = student.sid;
                document.getElementById('sno').value = student.sno;
                document.getElementById('name').value = student.name;
                document.getElementById('sfzh').value = student.sfzh;
                document.getElementById('gender').value = student.gender;
                document.getElementById('major').value = student.major;
                document.getElementById('remark').value = student.remark || '';
            } else {
                alert('获取学生信息失败：' + result.message);
            }
        })
        .catch(error => alert('获取学生信息失败：' + error));
}

// 删除学生
function deleteStudent(sid) {
    if (confirm('确定要删除这个学生吗？')) {
        fetch(`/api/students/${sid}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                alert('删除成功');
                loadStudents();
            } else {
                alert('删除失败：' + result.message);
            }
        })
        .catch(error => alert('删除失败：' + error));
    }
}

// 重置表单
function resetForm() {
    document.getElementById('sid').value = '';
    document.getElementById('sno').value = '';
    document.getElementById('name').value = '';
    document.getElementById('sfzh').value = '';
    document.getElementById('gender').value = 'M';
    document.getElementById('major').value = '';
    document.getElementById('remark').value = '';
} 