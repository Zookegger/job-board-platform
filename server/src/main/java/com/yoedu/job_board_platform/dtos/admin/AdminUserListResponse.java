package com.yoedu.job_board_platform.dtos.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminUserListResponse(
        @Schema(description = "ID tài khoản")
        UUID id,

        @Schema(description = "Email đăng nhập của tài khoản")
        String email,

        @Schema(description = "Họ tên người dùng, lấy từ profile nếu có")
        String fullName,

        @Schema(description = "Vai trò người dùng: ADMIN, EMPLOYER, CANDIDATE")
        UserRole role,

        @Schema(description = "Trạng thái tài khoản: ACTIVE hoặc INACTIVE")
        String status,

        @Schema(description = "Tài khoản có đang hoạt động hay không")
        boolean isActive,

        @Schema(description = "Thời điểm tạo tài khoản")
        OffsetDateTime createdAt
) {
}