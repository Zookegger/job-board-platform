package com.yoedu.job_board_platform.common.exceptions;

/**
 * Ngoại lệ cho request không hợp lệ (HTTP 400).
 * Sử dụng khi dữ liệu đầu vào không đúng định dạng hoặc thiếu thông tin bắt buộc.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
