# 文件上传安全性指南

## 路径遍历攻击（Path Traversal）

路径遍历是文件上传功能中的一个严重安全漏洞。攻击者可能通过文件名包含 `../` 或使用绝对路径如 `/etc/passwd` 来尝试访问或覆盖服务器上的敏感文件。

### 攻击示例

攻击者可能尝试以下攻击手段：

1. 上传文件名为 `../../../etc/passwd` 的文件
2. 上传文件名为 `../../../../etc/shadow` 的文件
3. 上传看似正常但包含路径操作的文件名：`image%2E%2E%2F%2E%2E%2Fpasswd.jpg`（URL编码的`../`)
4. 利用Windows与Linux路径分隔符差异：`..\\..\\windows\\system32\\config\\sam`
5. 利用空字节注入绕过后缀检查：`malicious.php\0.jpg`

### 防范措施

#### 1. 文件名安全处理

最主要的防范措施是**完全忽略用户提供的文件名**，而是使用系统生成的安全文件名：

```java
// 不安全：直接使用用户上传的文件名
String unsafePath = uploadDir + "/" + file.getOriginalFilename(); // 危险！

// 安全：生成新的随机文件名，只保留原始扩展名
String originalFilename = file.getOriginalFilename();
String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
String safeFilename = UUID.randomUUID().toString() + extension;
```

#### 2. 规范化路径并验证

即使使用了自定义文件名，也应该对最终路径进行规范化和验证：

```java
// 创建目标文件路径
Path targetPath = uploadDir.resolve(safeFilename).normalize();

// 验证最终路径是否仍在允许的目录内
if (!targetPath.startsWith(uploadDir)) {
    throw new SecurityException("检测到路径遍历攻击尝试");
}
```

#### 3. 使用安全API

Java中的`Path`类比直接操作字符串更安全：

```java
// 安全方法
Path uploadDir = Paths.get("/safe/upload/directory");
Path targetPath = uploadDir.resolve(safeFilename).normalize();
Files.copy(file.getInputStream(), targetPath);

// 而不是这样
// String filePath = "/safe/upload/directory/" + filename; // 危险！
// FileOutputStream fos = new FileOutputStream(filePath);
```

#### 4. 验证文件内容类型

除了验证路径，还应该验证文件的实际内容：

```java
// 使用Apache Tika验证文件内容类型
Tika tika = new Tika();
String detectedType = tika.detect(file.getInputStream());
if (!allowedMimeTypes.contains(detectedType)) {
    throw new SecurityException("文件类型不允许");
}
```

#### 5. 使用专门的目录并限制权限

```java
// 设置上传目录的权限
Path uploadDir = Paths.get(uploadPath);
if (Files.exists(uploadDir)) {
    // 仅给予最低权限
    Files.setPosixFilePermissions(uploadDir, 
        EnumSet.of(PosixFilePermission.OWNER_READ, 
                   PosixFilePermission.OWNER_WRITE,
                   PosixFilePermission.OWNER_EXECUTE));
}
```

### 实际示例：完整的防御代码

```java
@PostMapping("/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        // 1. 获取并验证文件扩展名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                return ResponseEntity.badRequest().body("不支持的文件类型");
            }
        }
        
        // 2. 生成安全的文件名 (UUID + 扩展名)
        String safeFilename = UUID.randomUUID().toString() + extension;
        
        // 3. 解析和规范化路径
        Path uploadDir = Paths.get(this.uploadDir).toAbsolutePath().normalize();
        Path targetPath = uploadDir.resolve(safeFilename).normalize();
        
        // 4. 验证最终路径是否在允许的目录内
        if (!targetPath.startsWith(uploadDir)) {
            return ResponseEntity.badRequest().body("检测到路径遍历攻击");
        }
        
        // 5. 确保上传目录存在
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // 6. 验证文件内容类型
        try (InputStream is = file.getInputStream()) {
            Tika tika = new Tika();
            String detectedType = tika.detect(is);
            if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
                return ResponseEntity.badRequest().body("文件内容类型不支持");
            }
        }
        
        // 7. 安全地保存文件
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // 8. 返回成功信息
        return ResponseEntity.ok("文件上传成功: " + safeFilename);
        
    } catch (IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("文件上传失败: " + ex.getMessage());
    }
}
```

## 其他文件上传安全威胁

除了路径遍历攻击外，文件上传功能还面临以下安全威胁：

### 1. 文件类型欺骗

攻击者可能尝试将恶意脚本文件(如PHP文件)伪装成图片上传：

```
malicious.php.jpg  // 双扩展名
malicious.jpg      // 实际是PHP文件，只修改了扩展名
```

**防御措施**：
- 不仅检查扩展名，还要检查文件内容的Magic Numbers
- 使用Apache Tika或类似工具验证文件MIME类型
- 将上传目录配置为不执行脚本文件

### 2. 文件大小攻击

攻击者可能上传非常大的文件，导致磁盘空间耗尽或服务器内存溢出。

**防御措施**：
- 设置最大文件大小限制
- 在前端和后端都进行文件大小验证
- 使用流式处理大文件，避免内存溢出

### 3. 元数据利用

某些文件格式(如EXIF图片)可能包含可执行代码：

**防御措施**：
- 清除上传文件的元数据
- 对图片文件进行重新处理，生成新图片

### 4. 存储型XSS攻击

SVG图像文件可能包含JavaScript代码，导致XSS攻击：

**防御措施**：
- 对SVG文件进行特殊处理，过滤掉脚本标签
- 使用Content-Disposition响应头，阻止在浏览器中直接执行

## 安全最佳实践总结

1. **永不信任用户输入**：包括文件名、文件内容和文件类型
2. **使用白名单**：明确允许的文件类型，拒绝所有其他类型
3. **多层防御**：同时验证扩展名和文件内容
4. **安全命名**：使用随机生成的文件名存储上传文件
5. **权限控制**：合理设置上传目录的权限
6. **独立存储**：将上传文件存储在与应用分离的位置
7. **定期清理**：删除长期未使用的上传文件
8. **监控异常**：记录和报警可疑的上传行为

## 安全与可识别性平衡：文件命名策略

纯UUID命名虽然安全，但会造成文件难以识别和管理的问题。以下介绍几种能同时兼顾安全性和可识别性的文件命名策略：

### 1. 前缀+UUID命名法

```java
// 为不同类型文件添加前缀
String filePrefix = "";
if (fileType.equals("avatar")) {
    filePrefix = "avatar_";
} else if (fileType.equals("document")) {
    filePrefix = "doc_";
}

String filename = filePrefix + UUID.randomUUID().toString() + extension;
// 例如: avatar_550e8400-e29b-41d4-a716-446655440000.jpg
```

**优点**：
- 通过前缀快速识别文件类型
- 维持UUID的唯一性和随机性
- 简单易实现

### 2. 用户标识+UUID命名法

```java
// 使用用户ID(或其哈希值)作为命名的一部分
String userId = currentUser.getUserId();
// 可选: 对用户ID进行哈希处理提高安全性
String userHash = DigestUtils.md5Hex(userId).substring(0, 8);

String filename = userHash + "_" + UUID.randomUUID().toString() + extension;
// 例如: 7b21e3d8_550e8400-e29b-41d4-a716-446655440000.jpg
```

**优点**：
- 可以根据文件名快速识别所属用户
- 用户ID哈希化后不易被猜测
- 便于按用户归类管理文件

### 3. 分类目录+命名策略

```java
// 根据上传类型和用户确定存储目录
String basePath = "/uploads";
String typePath = fileType; // "avatar", "document"等
String userIdHash = DigestUtils.md5Hex(userId).substring(0, 8);
// 生成目录结构: /uploads/avatar/7b21e3d8/
Path directory = Paths.get(basePath, typePath, userIdHash);

// 文件名使用简单UUID
String filename = UUID.randomUUID().toString() + extension;
Path fullPath = directory.resolve(filename);
```

**优点**：
- 文件名保持简单
- 通过目录结构实现分类和用户识别
- 更符合文件系统管理习惯

### 4. 时间戳+序列号命名法

```java
// 获取当前时间戳
String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
// 获取用户上传序列号（可从数据库或缓存获取）
int sequence = userUploadCountService.getAndIncrementCount(userId);

String filename = timestamp + "_" + userId.substring(0, 4) + "_" + 
                   sequence + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
// 例如: 20250408_135045_user_42_550e8400.jpg
```

**优点**：
- 包含上传时间信息，便于时间顺序管理
- 包含用户上传序号，可了解用户行为
- 仍保留UUID部分确保唯一性
- 文件名直观包含多项信息

### 5. 数据库映射策略

采用一个简单UUID命名文件，但在数据库中维护映射关系：

```java
// 文件仍使用UUID命名
String uuid = UUID.randomUUID().toString();
String filename = uuid + extension;

// 但在数据库中记录完整信息
fileRepository.save(new FileEntity(
    uuid,                   // 存储ID
    originalFilename,       // 原始文件名
    filename,               // 存储文件名
    userId,                 // 上传用户
    fileType,               // 文件类型
    new Date(),             // 上传时间
    file.getSize(),         // 文件大小
    filePath                // 存储路径
));
```

**优点**：
- 文件名保持简单安全
- 通过数据库可以实现任意复杂的查询和关联
- 更适合需要高级文件管理功能的系统

### 6. 混合策略示例（推荐实践）

以下是一个综合了以上多种方法的混合策略：

```java
// 1. 创建用户相关目录结构
String userDir = DigestUtils.md5Hex(userId).substring(0, 2); // 用户目录前2位哈希
String categoryDir = fileType;  // 例如"avatar"
Path directory = Paths.get(baseUploadDir, categoryDir, userDir, userId);
Files.createDirectories(directory);

// 2. 生成有标识性的文件名
String timestamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
// 用户8位哈希
String userHash = DigestUtils.md5Hex(userId).substring(0, 8);
// 8位UUID
String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
// timestamp_userHash_category_shortUuid.jpg
String filename = timestamp + "_" + userHash + "_" + fileType.substring(0, 3) + 
                  "_" + shortUuid + extension;

// 3. 存储文件
Path filePath = directory.resolve(filename);
Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

// 4. 记录到数据库，便于查询
fileRepository.save(new FileEntity(
    shortUuid,              // 文件标识符
    originalFilename,       // 原始文件名
    filename,               // 存储文件名
    userId,                 // 上传用户
    fileType,               // 文件类型
    filePath.toString(),    // 完整路径
    file.getSize()          // 文件大小
));
```

**最终存储结构示例**：
```
/uploads/
  /avatar/
    /7b/
      /user12345/
        20250408_7b21e3d8_ava_550e8400.jpg
  /document/
    /7b/
      /user12345/
        20250408_7b21e3d8_doc_a716446.pdf
```

#### 安全注意事项

无论采用哪种命名策略，都需要注意：

1. **仍然要验证最终路径**：确保生成的路径不会导致目录遍历
2. **对用户ID进行哈希处理**：不要在文件名中直接使用明文用户ID
3. **权限控制**：确保用户只能访问自己的文件
4. **正确配置Web服务器**：上传目录不应执行脚本文件
