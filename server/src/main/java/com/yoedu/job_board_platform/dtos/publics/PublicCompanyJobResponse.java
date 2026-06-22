package com.yoedu.job_board_platform.dtos.publics;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicCompanyJobResponse(
        @Schema(description = "ID việc làm", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Tiêu đề việc làm", example = "Lập trình viên Java")
        String title,

        @Schema(description = "Địa điểm làm việc", example = "Hà Nội")
        String location,

        @Schema(description = "Trạng thái", example = "ACTIVE")
        String status,

        @Schema(description = "Ngày tạo", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime createdAt
) {
}
