package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;

/**
 * Service quản lý refresh token.
 * Hỗ trợ tạo, xác thực, thu hồi token và dọn dẹp token hết hạn.
 */
public interface RefreshTokenService {

    /**
     * Tạo mới một refresh token cho người dùng. Token sẽ có thời hạn và trạng thái
     * không bị thu hồi. Sau đó lưu vào cơ sở dữ liệu và trả về đối tượng
     * RefreshToken đã lưu.
     *
     * @param user người dùng cần tạo refresh token
     * @return RefreshToken đối tượng refresh token đã được tạo và lưu vào cơ sở dữ liệu
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Xác thực một refresh token dựa trên chuỗi token. Kiểm tra xem token có tồn
     * tại, không bị thu hồi và chưa hết hạn hay không. Nếu hợp lệ, trả về đối tượng
     * RefreshToken tương ứng. Nếu không hợp lệ, ném ra BadRequestException với
     * thông báo lỗi phù hợp.
     *
     * @param token chuỗi refresh token cần xác thực
     * @return RefreshToken đối tượng refresh token nếu token hợp lệ
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Thu hồi một refresh token dựa trên chuỗi token. Tìm token trong cơ sở dữ
     * liệu, nếu tồn tại thì đánh dấu là đã bị thu hồi và lưu lại. Nếu token không
     * tồn tại, ném ra BadRequestException với thông báo lỗi phù hợp.
     *
     * @param token chuỗi refresh token cần thu hồi
     */
    void revokeRefreshToken(String token);

    /**
     * Thu hồi tất cả refresh token của một người dùng dựa trên ID người dùng. Tìm
     * tất cả token liên quan đến người dùng trong cơ sở dữ liệu, đánh dấu tất cả là
     * đã bị thu hồi và lưu lại.
     *
     * @param userId ID của người dùng cần thu hồi token
     */
    void revokeAllUserTokens(UUID userId);

    /**
     * Dọn dẹp các refresh token đã hết hạn trong hệ thống.
     * Phương thức được Spring Scheduler tự động thực thi vào lúc 03:00 sáng
     * mỗi ngày. Tất cả các token có thời gian hết hạn nhỏ hơn thời điểm hiện tại
     * sẽ bị xóa khỏi cơ sở dữ liệu.
     */
    void cleanupExpiredTokens();
}
