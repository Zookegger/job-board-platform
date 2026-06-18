package com.yoedu.job_board_platform.dtos.company;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO để từ chối phê duyệt công ty.
 * Bắt buộc phải có lý do từ chối.
 */
public record CompanyRejectionRequest(
        @NotBlank(message = "Company ID không được để trống")
        String companyId,

        @NotBlank(message = "Lý do từ chối không được để trống")
        String rejectionReason
) {}

