package com.yoedu.job_board_platform.dtos.company;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO để tạm ngưng công ty.
 * Bắt buộc phải có lý do tạm ngưng.
 */
public record CompanySuspensionRequest(
        @NotBlank(message = "Company ID không được để trống")
        String companyId,

        @NotBlank(message = "Lý do tạm ngưng không được để trống")
        String suspensionReason
) {}

