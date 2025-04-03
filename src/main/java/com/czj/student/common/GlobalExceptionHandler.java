package com.czj.student.common;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return ApiResponse.paramError(message);
    }

    // 参数绑定异常
    @ExceptionHandler(BindException.class)
    public ApiResponse<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败：{}", message);
        return ApiResponse.paramError(message);
    }

    // 业务异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务校验失败：{}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    // 其他未知异常
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return ApiResponse.error("系统繁忙，请稍后重试");
    }
}
