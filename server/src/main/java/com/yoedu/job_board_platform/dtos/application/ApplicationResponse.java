package com.yoedu.job_board_platform.dtos.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplicationResponse(
        @Schema(description = "ID đơn ứng tuyển") UUID id,
        @Schema(description = "ID tin tuyển dụng") UUID jobId,
        @Schema(description = "Tiêu đề tin tuyển dụng") String jobTitle,
        @Schema(description = "Tên công ty") String companyName,
        @Schema(description = "Trạng thái đơn") ApplicationStatus status,
        @Schema(description = "Thư ứng tuyển") String coverLetter,
        @Schema(description = "Ngày nộp đơn") OffsetDateTime appliedAt
) {
}
