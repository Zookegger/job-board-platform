package com.yoedu.job_board_platform.common.exceptions;

/**
 * Ngoại lệ cho truy cập bị từ chối (HTTP 403).
 * Sử dụng khi người dùng không có quyền thực hiện hành động.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super((message.isEmpty() || message.isBlank()) ? "Người dùng không có quyền truy cập tài nguyên này" : message);
    }
}
