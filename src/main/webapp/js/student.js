// 分页相关的全局变量
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let keyword = '';

// 页面加载完成后获取学生列表
window.onload = function() {
    loadStudents();
};

// 加载学生列表
function loadStudents() {
    console.log('开始加载学生列表...');
    const url = `/api/students/page?page=${currentPage}&size=${pageSize}${keyword ? `&keyword=${encodeURIComponent(keyword)}` : ''}`;
    
    fetch(url, {
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
            const data = result.data;
            // 更新总页数
            totalPages = Math.ceil(data.total / pageSize);
            
            // 更新页面显示
            updatePageInfo();
            updatePageButtons();
            
            // 渲染学生列表
            const tbody = document.getElementById('studentList');
            tbody.innerHTML = '';
            data.rows.forEach(student => {
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

// 更新页面信息显示
function updatePageInfo() {
    document.getElementById('pageInfo').textContent = `第 ${currentPage} 页 / 共 ${totalPages} 页`;
}

// 更新分页按钮状态
function updatePageButtons() {
    document.getElementById('prevBtn').disabled = currentPage <= 1;
    document.getElementById('nextBtn').disabled = currentPage >= totalPages;
}

// 切换页码
function changePage(page) {
    if (page >= 1 && page <= totalPages) {
        currentPage = page;
        loadStudents();
    }
}

// 改变每页显示数量
function changePageSize() {
    pageSize = parseInt(document.getElementById('pageSize').value);
    currentPage = 1; // 重置到第一页
    loadStudents();
}

// 搜索学生
function searchStudents() {
    keyword = document.getElementById('searchKeyword').value.trim();
    currentPage = 1; // 重置到第一页
    loadStudents();
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
            currentPage = 1; // 重置到第一页
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
                // 如果当前页没有数据了，就回到上一页
                if (currentPage > 1 && document.getElementById('studentList').children.length === 1) {
                    currentPage--;
                }
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