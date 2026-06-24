package com.yoedu.job_board_platform.dtos.job;

import io.swagger.v3.oas.annotations.media.Schema;

public record JobCategoryResponse(@Schema(description = "ID ngành nghề", example = "1") Integer id,

        @Schema(description = "Tên ngành nghề", example = "IT") String name) {

}
