package com.yoedu.job_board_platform.dtos.skill;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminSkillResponse(
        @Schema(description = "ID kỹ năng", example = "1") Integer id,

        @Schema(description = "Tên kỹ năng", example = "Java") String name,

        @Schema(description = "Kỹ năng có đang hoạt động hay không", example = "true") boolean isActive,

        @Schema(description = "", example = "") OffsetDateTime createdAt,
        @Schema(description = "", example = "") OffsetDateTime updatedAt) {

}
