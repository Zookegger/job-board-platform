package com.yoedu.job_board_platform.dtos.application;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả kiểm tra ứng viên đã nộp đơn chưa")
public record ApplicationCheckResponse(
        @Schema(description = "Đã nộp đơn chưa") boolean applied,
        @Schema(description = "UUID của đơn (null nếu chưa nộp)") UUID applicationId) {
}
