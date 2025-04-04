# @ModelAttribute 注解使用说明

## 1. 简介
`@ModelAttribute` 是 Spring MVC 框架中的一个重要注解，主要用于在控制器（Controller）中处理模型数据。它可以应用在方法或方法参数上，用于数据绑定和预处理。

## 2. 使用场景

### 2.1 方法级注解
在方法上使用 `@ModelAttribute` 时，该方法会在当前控制器的每个请求处理方法执行之前被调用。

```java
@ModelAttribute
public void populateModel(@RequestParam(required = false) Long id, Model model) {
    if (id != null) {
        // 预先加载数据
        model.addAttribute("data", service.findById(id));
    }
}
```

### 2.2 参数级注解
用于方法参数时，会将请求参数自动绑定到对象属性。

```java
@PutMapping("/{id}")
public ApiResponse<Void> update(@PathVariable Long id, @ModelAttribute Entity entity) {
    entity.setId(id);
    service.update(entity);
    return ApiResponse.success();
}
```

## 3. 常见用途

### 3.1 预加载数据
```java
@ModelAttribute
public void getStudent(@RequestParam(required = false) Long sid, Model model) {
    if (sid != null && !model.containsAttribute("student")) {
        model.addAttribute("student", studentService.getStudentById(sid));
    }
}
```

### 3.2 提供公共数据
```java
@ModelAttribute("majors")
public List<String> populateMajors() {
    return Arrays.asList(
        "计算机科学与技术",
        "软件工程",
        "人工智能"
    );
}
```

### 3.3 表单数据绑定
```java
@PostMapping("/save")
public String save(@ModelAttribute("student") Student student, BindingResult result) {
    if (result.hasErrors()) {
        return "student/form";
    }
    studentService.save(student);
    return "redirect:/students";
}
```

## 4. 注意事项

1. **执行时机**
   - 带有 `@ModelAttribute` 注解的方法会在 Controller 的每个请求处理方法执行前被调用
   - 多个 `@ModelAttribute` 方法按照方法名的字母顺序执行

2. **性能考虑**
   - 避免在 `@ModelAttribute` 方法中执行耗时操作
   - 对于不经常变化的数据，考虑使用缓存
   - 只加载必要的数据

3. **数据覆盖**
   - 注意同名属性可能造成的数据覆盖问题
   - 使用 `model.containsAttribute()` 检查是否已存在数据

4. **RESTful API 考虑**
   - 在构建 RESTful API 时，通常不需要使用 `@ModelAttribute`
   - 对于简单的数据绑定，直接使用 `@RequestBody` 更合适

## 5. 最佳实践

### 5.1 合理使用
```java
@Controller
@RequestMapping("/students")
public class StudentController {
    @ModelAttribute
    public void populateFormData(Model model) {
        // 只在表单相关的请求中使用
        if (!model.containsAttribute("majors")) {
            model.addAttribute("majors", getMajorList());
        }
    }
}
```

### 5.2 避免过度使用
```java
// 不推荐
@ModelAttribute
public void loadEverything(Model model) {
    // 加载过多不必要的数据
    model.addAttribute("allStudents", studentService.findAll());
    model.addAttribute("allCourses", courseService.findAll());
    // ...
}
```

### 5.3 结合数据验证
```java
@PostMapping("/save")
public String save(@ModelAttribute @Valid Student student, 
                  BindingResult result, 
                  Model model) {
    if (result.hasErrors()) {
        // 验证失败时保留已输入的数据
        return "student/form";
    }
    // ...
} 