package com.yoedu.job_board_platform.dtos.skill;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bộ lọc tìm kiếm danh sách kỹ năng")
public record SkillFilterRequest(
                @Schema(description = "Từ khóa tìm kiếm theo tên kỹ năng", example = "Java") String keyword,

                @Schema(description = "Trạng thái kích hoạt (null: tất cả, true: hoạt động, false: khóa)", example = "true") Boolean isActive) {
}