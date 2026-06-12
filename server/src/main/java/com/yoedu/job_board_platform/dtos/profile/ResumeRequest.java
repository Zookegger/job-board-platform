package com.yoedu.job_board_platform.dtos.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record ResumeRequest(
        @Schema(description = "Tiêu đề resume", example = "Nguyễn Văn A - Software Engineer")
        @Size(max = 255) String title
) {
}
