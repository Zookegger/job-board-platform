package com.yoedu.job_board_platform.dtos.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record CandidateProfileRequest(
        @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
        @Size(max = 100) String fullName,

        @Schema(description = "Số điện thoại", example = "0901234567")
        @Size(max = 15) String phone,

        @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
        @Size(max = 2048) String avatarUrl
) {
}
