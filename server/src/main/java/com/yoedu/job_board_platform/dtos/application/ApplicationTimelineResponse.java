package com.yoedu.job_board_platform.dtos.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yoedu.job_board_platform.models.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationTimelineResponse(
                @Schema(description = "ID bản ghi trạng thái") UUID id,
                @Schema(description = "Trạng thái mới") ApplicationStatus status,
                @Schema(description = "Nhãn trạng thái") String statusLabel,
                @Schema(description = "Tên người thay đổi") String changedByName,
                @Schema(description = "Ghi chú") String note,
                @Schema(description = "Thời gian thay đổi") OffsetDateTime changedAt) {
}
