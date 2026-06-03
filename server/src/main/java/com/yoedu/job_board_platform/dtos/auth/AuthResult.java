package com.yoedu.job_board_platform.dtos.auth;

import com.yoedu.job_board_platform.models.UserRole;

public record AuthResult(
    String accessToken, 
    String refreshToken, 
    UserRole role,
    long expiresIn) {
}
