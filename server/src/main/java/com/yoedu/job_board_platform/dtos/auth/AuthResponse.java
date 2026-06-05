package com.yoedu.job_board_platform.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Refresh token dùng để lấy access token mới", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
        String refreshToken,

        @Schema(description = "Loại token", example = "Bearer")
        String tokenType,

        @Schema(description = "Thời gian hết hạn của access token (ms)", example = "3600000")
        long expiresIn
) {}
