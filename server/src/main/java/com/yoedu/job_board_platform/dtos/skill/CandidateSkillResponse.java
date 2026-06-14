package com.yoedu.job_board_platform.dtos.skill;

import com.yoedu.job_board_platform.models.ProficientLevel;

import io.swagger.v3.oas.annotations.media.Schema;

public record CandidateSkillResponse(
        @Schema(description = "ID kỹ năng", example = "1")
        Integer skillId,

        @Schema(description = "Tên kỹ năng", example = "Java")
        String skillName,

        @Schema(description = "Mức độ thành thạo", example = "INTERMEDIATE")
        ProficientLevel proficientLevel
) {
}
