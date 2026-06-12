package com.yoedu.job_board_platform.dtos.profile;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record CandidateProfileResponse(
        @Schema(description = "ID người dùng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Email", example = "candidate@example.com")
        String email,

        @Schema(description = "Vai trò", example = "CANDIDATE")
        String role,

        @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
        String fullName,

        @Schema(description = "Số điện thoại", example = "0901234567")
        String phone,

        @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
        String avatarUrl
) {
}
