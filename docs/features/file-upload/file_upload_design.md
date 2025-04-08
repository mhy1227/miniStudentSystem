# 文件上传功能设计方案

## 1. 功能需求分析

文件上传功能（以头像为例）需要考虑以下基本需求：

- 用户头像上传与更新
- 文件类型限制和验证
- 文件大小限制
- 文件存储管理
- 图片处理（可选：裁剪、缩放等）
- 访问控制

## 2. 数据库设计

### 方案一：直接在学生表添加字段（简单实现）

需要在学生表中添加以下字段：
```sql
ALTER TABLE student ADD COLUMN avatar_path VARCHAR(255) COMMENT '头像路径';
ALTER TABLE student ADD COLUMN avatar_upload_time DATETIME COMMENT '头像上传时间';
```

### 方案二：独立的文件表（更灵活）

```sql
CREATE TABLE upload_file (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(255) NOT NULL COMMENT '存储路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    upload_time DATETIME NOT NULL COMMENT '上传时间',
    upload_user_id VARCHAR(50) NOT NULL COMMENT '上传用户ID',
    file_status TINYINT DEFAULT 1 COMMENT '文件状态：1-有效，0-已删除',
    file_md5 VARCHAR(32) COMMENT '文件MD5值，用于去重'
) COMMENT '上传文件表';

-- 关联表（如果需要多种文件类型）
CREATE TABLE user_avatar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL COMMENT '用户ID',
    file_id BIGINT NOT NULL COMMENT '文件ID',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (file_id) REFERENCES upload_file(file_id)
) COMMENT '用户头像关联表';
```

## 3. 文件存储策略

### 方案一：本地文件系统

```
/uploads
  /avatars
    /yyyy-MM-dd/          # 按日期组织
      user_id_timestamp.jpg
```

**配置文件项**：
```properties
# 文件上传配置
file.upload.root-dir=D:/uploads
file.upload.avatar-dir=/avatars
file.upload.max-size=5242880  # 5MB
file.upload.allowed-types=image/jpeg,image/png,image/gif
```

### 方案二：分布式存储（未来扩展）

- 对象存储服务：阿里云OSS、腾讯云COS等
- MinIO自建对象存储

## 4. 核心类设计

```
com.czj.student.upload/
├── config/
│   └── FileUploadConfig.java      # 上传配置类
├── controller/
│   └── FileUploadController.java  # 上传接口控制器
├── service/
│   ├── FileUploadService.java     # 上传服务接口
│   └── impl/
│       └── FileUploadServiceImpl.java  # 实现类
├── util/
│   └── FileUtil.java              # 文件处理工具
└── model/
    ├── dto/
    │   └── FileUploadDTO.java     # 上传请求
    └── vo/
        └── FileInfoVO.java        # 文件信息响应
```

## 5. 安全性考虑

- **文件类型验证**：不仅检查扩展名，还应检查文件内容的Magic Number
- **文件大小限制**：防止上传过大文件耗尽服务器资源
- **文件名安全处理**：使用UUID重命名，防止路径穿越漏洞
- **防止XSS攻击**：特别对SVG文件等可能包含脚本的文件进行过滤
- **访问控制**：防止未授权访问他人上传的文件

## 6. 接口设计

### 上传接口

```
POST /api/files/upload
Content-Type: multipart/form-data

Request:
- file: 文件内容
- type: 文件类型标识（如"avatar"、"document"等）

Response:
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileId": 123,
    "fileName": "原始文件名.jpg",
    "filePath": "/avatars/2025-04-08/user_001_1712345678.jpg",
    "fileUrl": "/api/files/123",
    "fileSize": 524288,
    "uploadTime": "2025-04-08 10:15:30"
  }
}
```

### 文件访问接口

```
GET /api/files/{fileId}
GET /api/files/download/{fileId}
GET /api/users/{userId}/avatar  # 获取用户头像便捷方法
```

## 7. 前端实现考虑

- 使用AJAX上传显示进度条
- 图片预览与裁剪功能
- 拖拽上传区域
- 文件类型与大小的前端验证

## 8. 开发路线

1. 创建数据库结构
2. 实现核心上传功能
3. 开发用户头像管理功能
4. 添加安全性措施
5. 完成前端集成

## 9. 风险与挑战

- 文件存储空间管理：随着用户增加，需要考虑存储空间扩展
- 性能优化：大文件上传可能影响系统性能
- 文件去重：考虑MD5识别重复文件减少存储空间浪费
- 并发上传：高并发场景下的文件名冲突处理 