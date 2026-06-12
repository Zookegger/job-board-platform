package com.yoedu.job_board_platform.common.exceptions;

/**
 * Ngoại lệ cho tài nguyên không tìm thấy (HTTP 404).
 * Sử dụng khi truy vấn ID không tồn tại trong hệ thống.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
