package com.yoedu.job_board_platform.dtos.admin;

import jakarta.validation.constraints.NotBlank;

public record JobRejectRequest(
        @NotBlank(message = "Lý do từ chối là bắt buộc") String reason) {
}
