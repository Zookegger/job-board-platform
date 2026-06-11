package com.yoedu.job_board_platform.common.exceptions;

/**
 * Ngoại lệ cho tài nguyên không tồn tại (HTTP 404).
 * Tương tự NotFoundException nhưng mang ngữ nghĩa "tài nguyên" cụ thể hơn.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
