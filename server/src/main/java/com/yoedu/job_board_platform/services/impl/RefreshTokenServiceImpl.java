package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.services.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Triển khai RefreshTokenService. Lưu token vào database,
 * kiểm tra hiệu lực và thu hồi token. Tự động dọn dẹp token hết hạn theo lịch.
 */
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.refresh-token.expiration-days:7}")
    private long refreshTokenExpirationDays;

    @Override
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(refreshTokenExpirationDays))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenString(token)
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại"));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token đã bị thu hồi");
        }

        if (OffsetDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            throw new BadRequestException("Refresh token đã hết hạn");
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenString(token)
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?") // 3h sáng mỗi ngày
    // @Scheduled(cron = "0 * * * * ?") // Mỗi 1 phút
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(OffsetDateTime.now());
    }
}
