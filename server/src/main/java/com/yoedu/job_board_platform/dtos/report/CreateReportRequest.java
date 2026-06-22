package com.yoedu.job_board_platform.dtos.report;

import java.util.UUID;

import com.yoedu.job_board_platform.models.ReportReason;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Yêu cầu tạo báo cáo vi phạm.
 * Người dùng báo cáo một bài tuyển dụng hoặc một công ty với lý do cụ thể.
 */
@Schema(description = "Yêu cầu tạo báo cáo vi phạm")
public record CreateReportRequest(
        @Schema(description = "ID bài tuyển dụng bị báo cáo (nullable nếu báo cáo công ty)")
        UUID jobId,

        @Schema(description = "ID công ty bị báo cáo (nullable nếu báo cáo bài tuyển dụng)")
        UUID companyId,

        @NotNull
        @Schema(description = "Lý do báo cáo: SPAM, SCAM, INAPPROPRIATE, OTHER")
        ReportReason reason,

        @Schema(description = "Chi tiết báo cáo (tuỳ chọn)", nullable = true)
        String details
) {
}
