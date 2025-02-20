package com.example.jwtdemo.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse<T> {
    private String timestamp;
    private int status;
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(200)
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(Class<T> type, T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(500)
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now().toString())
                .status(500)
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}
