package com.yoedu.job_board_platform.dtos.auth;

import com.yoedu.job_board_platform.models.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResult(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Refresh token", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
        String refreshToken,

        @Schema(description = "Vai trò người dùng", example = "CANDIDATE")
        UserRole role,

        @Schema(description = "Thời gian hết hạn access token (ms)", example = "3600000")
        long expiresIn) {
}
