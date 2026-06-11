package com.yoedu.job_board_platform.common.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Đối tượng phản hồi lỗi chuẩn cho API.
 * Chứa thời gian, mã trạng thái, thông báo lỗi và lỗi field cụ thể (nếu có).
 */
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(LocalDateTime timestamp, int status, String message, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}