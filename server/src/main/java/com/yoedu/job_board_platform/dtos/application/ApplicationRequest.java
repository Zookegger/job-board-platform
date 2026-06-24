package com.yoedu.job_board_platform.dtos.application;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu nộp đơn ứng tuyển")
public record ApplicationRequest(

        @Schema(description = "UUID của tin tuyển dụng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "jobId không được để trống")
        UUID jobId,

        @Schema(description = "Thư xin việc (tùy chọn, tối đa 5000 ký tự)", example = "Tôi rất muốn ứng tuyển vào vị trí này...")
        @Size(max = 5000, message = "Cover letter không được vượt quá 5000 ký tự")
        String coverLetter) {
}
