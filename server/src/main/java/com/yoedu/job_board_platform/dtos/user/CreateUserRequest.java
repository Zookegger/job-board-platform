package com.yoedu.job_board_platform.dtos.user;

import com.yoedu.job_board_platform.models.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Schema(description = "Email người dùng", example = "user@example.com")
        @Email @NotBlank @Size(max = 255) String email,

        @Schema(description = "Mật khẩu (ít nhất 8 ký tự)", example = "password123")
        @NotBlank @Size(min = 8, max = 255) String password,

        @Schema(description = "Vai trò người dùng: CANDIDATE, EMPLOYER, ADMIN", example = "CANDIDATE")
        @NotNull UserRole role,

        @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
        @NotBlank @Size(max = 100) String fullName,

        @Schema(description = "Số điện thoại", example = "0901234567")
        @NotBlank @Size(max = 15) String phone,

        @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
        @Size(max = 2048) String avatarUrl
) {
}
