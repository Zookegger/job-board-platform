package com.yoedu.job_board_platform.dtos.user;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(description = "ID người dùng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Email người dùng", example = "user@example.com")
        String email,

        @Schema(description = "Vai trò", example = "CANDIDATE")
        String role,

        @Schema(description = "Trạng thái hoạt động", example = "true")
        boolean isActive,

        @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
        String fullName,

        @Schema(description = "Avatar URL", example = "http://localhost:5000/uploads/avatars/12312.jpeg")
        String avatarUrl
) {
}
