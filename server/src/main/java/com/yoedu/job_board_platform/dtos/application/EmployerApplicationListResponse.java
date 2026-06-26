package com.yoedu.job_board_platform.dtos.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin hồ sơ ứng viên trong danh sách của nhà tuyển dụng")
public record EmployerApplicationListResponse(

        @Schema(description = "UUID của đơn ứng tuyển") UUID id,
        @Schema(description = "UUID của hồ sơ ứng viên") UUID candidateId,
        @Schema(description = "Họ tên ứng viên") String candidateName,
        @Schema(description = "Avatar ứng viên") String candidateAvatarUrl,
        @Schema(description = "Email ứng viên") String candidateEmail,
        @Schema(description = "UUID của tin tuyển dụng") UUID jobId,
        @Schema(description = "Tên tin tuyển dụng") String jobTitle,
        @Schema(description = "Trạng thái đơn") ApplicationStatus status,
        @Schema(description = "Thư xin việc") String coverLetter,
        @Schema(description = "URL CV") String resumeUrl,
        @Schema(description = "Thời điểm nộp đơn") OffsetDateTime appliedAt) {
}
