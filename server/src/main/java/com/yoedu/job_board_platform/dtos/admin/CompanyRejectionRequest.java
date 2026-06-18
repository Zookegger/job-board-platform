package com.yoedu.job_board_platform.dtos.admin;

import jakarta.validation.constraints.NotBlank;

public record CompanyRejectionRequest(
        @NotBlank(message = "Lý do từ chối không được để trống")
        String reason
) {
}