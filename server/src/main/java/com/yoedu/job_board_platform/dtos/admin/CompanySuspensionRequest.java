package com.yoedu.job_board_platform.dtos.admin;

import jakarta.validation.constraints.NotBlank;

public record CompanySuspensionRequest(
        @NotBlank(message = "Lý do tạm ngưng không được để trống")
        String reason
) {
}