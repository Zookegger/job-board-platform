package com.yoedu.job_board_platform.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "Refresh token hiện tại", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
        @NotBlank(message = "Refresh token không được để trống")
        String refreshToken
) {}
