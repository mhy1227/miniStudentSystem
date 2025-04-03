package com.czj.student.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    public static <T> ApiResponse<T> paramError(String message) {
        return error(400, message);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }
}
