package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;

public interface RefreshTokenService {
    /**
     * Create a new refresh token for a user
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Validate a refresh token string
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Revoke a refresh token
     */
    void revokeRefreshToken(String token);

    /**
     * Revoke all refresh tokens for a user
     */
    void revokeAllUserTokens(UUID userId);

    /**
     * Xóa token hết hạn
     */
    void cleanupExpiredTokens();
}
