# git 提交步骤
1.添加文件到暂存区
```
# 添加所有修改的文件
git add src/main/java/com/czj/student/controller/StudentController.java
git add src/main/java/com/czj/student/mapper/StudentMapper.java
git add src/main/java/com/czj/student/service/StudentService.java
git add src/main/java/com/czj/student/service/impl/StudentServiceImpl.java
git add src/main/resources/mapper/StudentMapper.xml

# 添加所有新增的文件
git add src/main/java/com/czj/student/annotation/CacheInvalidate.java
git add src/main/java/com/czj/student/annotation/CacheQuery.java
git add src/main/java/com/czj/student/annotation/PageQuery.java
git add src/main/java/com/czj/student/aspect/CacheInvalidateAspect.java
git add src/main/java/com/czj/student/aspect/QueryAspect.java
git add src/main/java/com/czj/student/model/dto/
git add src/main/java/com/czj/student/model/vo/PageInfo.java
git add src/main/java/com/czj/student/model/vo/StudentVO.java
git add src/main/java/com/czj/student/util/pool/
```
2.提交更改
格式：
```
<type>: <subject>

[optional body]
```
分页功能的：
```
git commit -m "feat: 实现分页查询功能

实现了基于缓存的分页查询功能，主要包括以下内容：
1. 创建了PageInfo、StudentVO和StudentDTO类
2. 实现了QueryPool缓存池，支持查询结果缓存
3. 实现了基于AOP的缓存机制（PageQuery和CacheInvalidate注解）
4. 在StudentMapper中添加了分页查询方法
5. 在StudentService中实现了缓存分页查询
6. 在StudentController中添加了新的分页查询API"
```
3.提交到远程
```
git push origin feature/pagination
```
