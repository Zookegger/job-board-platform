package com.yoedu.job_board_platform.dtos.skill;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateCandidateSkillsRequest(
        @NotNull
        @Valid
        @Schema(description = "Danh sách kỹ năng muốn lưu")
        List<CandidateSkillItem> skills
) {
    public record CandidateSkillItem(
            @NotNull
            @Schema(description = "ID kỹ năng", example = "1")
            Integer skillId,

            @NotNull
            @Schema(description = "Mức độ thành thạo", example = "INTERMEDIATE")
            com.yoedu.job_board_platform.models.ProficientLevel proficientLevel
    ) {
    }
}
