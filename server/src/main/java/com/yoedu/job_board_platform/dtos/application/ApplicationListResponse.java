package com.yoedu.job_board_platform.dtos.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yoedu.job_board_platform.models.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationListResponse(
                @Schema(description = "ID đơn ứng tuyển") UUID id,
                @Schema(description = "ID công việc đã ứng tuyển") UUID jobId,
                @Schema(description = "Tiêu đề công việc") String jobTitle,
                @Schema(description = "Tên công ty") String companyName,
                @Schema(description = "Logo công ty") String companyLogoUrl,
                @Schema(description = "Địa điểm làm việc") String jobLocation,
                @Schema(description = "Trạng thái đơn ứng tuyển") ApplicationStatus status,
                @Schema(description = "Ngày nộp đơn") OffsetDateTime appliedAt) {
}
