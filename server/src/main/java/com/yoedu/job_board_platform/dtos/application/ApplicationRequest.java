package com.yoedu.job_board_platform.dtos.application;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull(message = "jobId không được để trống")
        @Schema(description = "ID tin tuyển dụng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID jobId,

        @Size(max = 5000, message = "Cover letter không được vượt quá 5000 ký tự")
        @Schema(description = "Thư ứng tuyển (cover letter)", example = "Kính gửi nhà tuyển dụng...")
        String coverLetter
) {
}
