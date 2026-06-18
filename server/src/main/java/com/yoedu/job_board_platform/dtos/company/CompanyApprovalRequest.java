package com.yoedu.job_board_platform.dtos.company;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO để phê duyệt công ty.
 */
public record CompanyApprovalRequest(
        @NotBlank(message = "Company ID không được để trống")
        String companyId
) {}

