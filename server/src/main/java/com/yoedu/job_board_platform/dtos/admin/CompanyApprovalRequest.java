package com.yoedu.job_board_platform.dtos.admin;

import jakarta.validation.constraints.NotBlank;

public record CompanyApprovalRequest(
        @NotBlank(message = "Lý do phê duyệt không được để trống")
        String reason
) {
}