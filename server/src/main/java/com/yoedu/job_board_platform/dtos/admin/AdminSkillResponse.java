package com.yoedu.job_board_platform.dtos.admin;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminSkillResponse(
        @Schema(description = "ID kỹ năng", example = "1")
        Integer id,

        @Schema(description = "Tên kỹ năng", example = "Java")
        String name,

        @Schema(description = "Kỹ năng có đang hoạt động hay không", example = "true")
        boolean isActive,

        @Schema(description = "Ngày tạo", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime createdAt,

        @Schema(description = "Ngày cập nhật", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime updatedAt
) {
}
