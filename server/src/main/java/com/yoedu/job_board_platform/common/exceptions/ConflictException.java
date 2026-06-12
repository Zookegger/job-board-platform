package com.yoedu.job_board_platform.common.exceptions;

/**
 * Ngoại lệ cho xung đột dữ liệu (HTTP 409).
 * Sử dụng khi có vi phạm ràng buộc duy nhất hoặc trạng thái không cho phép thao tác.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}