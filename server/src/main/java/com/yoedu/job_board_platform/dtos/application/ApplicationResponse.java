package com.yoedu.job_board_platform.dtos.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin đơn ứng tuyển")
public record ApplicationResponse(

                @Schema(description = "UUID của đơn ứng tuyển") UUID id,

                @Schema(description = "UUID của tin tuyển dụng") UUID jobId,

                @Schema(description = "Tên công việc") String jobTitle,

                @Schema(description = "Tên công ty") String companyName,

                @Schema(description = "Trạng thái đơn") ApplicationStatus status,

                @Schema(description = "Thư xin việc") String coverLetter,

                @Schema(description = "URL của CV đính kèm lúc nộp") String resumeUrl,

                @Schema(description = "Thời điểm nộp đơn") OffsetDateTime appliedAt) {
}
