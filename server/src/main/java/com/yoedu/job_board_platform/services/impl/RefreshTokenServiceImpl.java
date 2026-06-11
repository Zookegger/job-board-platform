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

    /**
     * Tạo mới một refresh token cho người dùng. Token sẽ có thời hạn và trạng thái
     * không bị thu hồi. Sau đó lưu vào cơ sở dữ liệu và trả về đối tượng
     * RefreshToken đã lưu.
     * 
     * @param user người dùng cần tạo refresh token
     * @return RefreshToken đối tượng refresh token đã được tạo và lưu vào cơ sở dữ
     *         liệu
     * @throws BadRequestException nếu có lỗi trong quá trình tạo token
     */
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

    /**
     * Xác thực một refresh token dựa trên chuỗi token. Kiểm tra xem token có tồn
     * tại, không bị thu hồi và chưa hết hạn hay không. Nếu hợp lệ, trả về đối tượng
     * RefreshToken tương ứng. Nếu không hợp lệ, ném ra BadRequestException với
     * thông báo lỗi phù hợp.
     * 
     * @param token chuỗi refresh token cần xác thực
     * @return RefreshToken đối tượng refresh token nếu token hợp lệ
     * @throws BadRequestException nếu token không tồn tại, đã bị thu hồi hoặc đã
     *                             hết hạn
     */
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

    /**
     * Thu hồi một refresh token dựa trên chuỗi token. Tìm token trong cơ sở dữ
     * liệu, nếu tồn tại thì đánh dấu là đã bị thu hồi và lưu lại. Nếu token không
     * tồn tại, ném ra BadRequestException với thông báo lỗi phù hợp.
     * 
     * @param token chuỗi refresh token cần thu hồi
     * @throws BadRequestException nếu token không tồn tại
     */
    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenString(token)
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Thu hồi tất cả refresh token của một người dùng dựa trên ID người dùng. Tìm
     * tất cả token liên quan đến người dùng trong cơ sở dữ liệu, đánh dấu tất cả là
     * đã bị thu hồi và lưu lại.
     * 
     * @param userId ID của người dùng cần thu hồi token
     */
    @Override
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Dọn dẹp các refresh token đã hết hạn trong hệ thống.
     *
     * <p>
     * Phương thức được Spring Scheduler tự động thực thi vào lúc 03:00 sáng
     * mỗi ngày. Tất cả các token có thời gian hết hạn ({@code expiresAt})
     * nhỏ hơn thời điểm hiện tại sẽ bị xóa khỏi cơ sở dữ liệu.
     * </p>
     *
     * <p>
     * Mục đích:
     * <ul>
     * <li>Loại bỏ các token không còn hợp lệ.</li>
     * <li>Giảm dung lượng lưu trữ không cần thiết trong cơ sở dữ liệu.</li>
     * <li>Tăng hiệu năng cho các thao tác liên quan đến refresh token.</li>
     * </ul>
     * </p>
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?") // 3h sáng mỗi ngày
    // @Scheduled(cron = "0 * * * * ?") // Mỗi 1 phút
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(OffsetDateTime.now());
    }
}
