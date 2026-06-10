package com.yoedu.job_board_platform.dtos.profile;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmployerProfileResponse(
        @Schema(description = "ID người dùng", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Email", example = "employer@company.com")
        String email,

        @Schema(description = "Vai trò", example = "EMPLOYER")
        String role,

        @Schema(description = "Họ và tên", example = "Trần Thị B")
        String fullName,

        @Schema(description = "Số điện thoại", example = "0912345678")
        String phone,

        @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
        String avatarUrl,

        @Schema(description = "ID công ty", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID companyId,

        @Schema(description = "Tên công ty", example = "Công ty TNHH ABC")
        String companyName,

        @Schema(description = "Vai trò trong công ty", example = "HR Manager")
        String roleInCompany
) {
}
