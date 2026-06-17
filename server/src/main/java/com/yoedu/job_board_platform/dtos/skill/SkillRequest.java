package com.yoedu.job_board_platform.dtos.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu tạo kỹ năng mới")
public record SkillRequest(
        @Schema(description = "Tên kỹ năng", example = "Kotlin") @NotBlank(message = "Tên kỹ năng không được để trống") @Size(max = 100, message = "Tên kỹ năng không được vượt quá 100 ký tự") String name,

        Boolean isActive) {
}
