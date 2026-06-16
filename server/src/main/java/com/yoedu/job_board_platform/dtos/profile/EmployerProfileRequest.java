package com.yoedu.job_board_platform.dtos.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record EmployerProfileRequest(
        @Schema(description = "Họ và tên", example = "Trần Thị B")
        @Size(max = 100) String fullName,

        @Schema(description = "Số điện thoại", example = "0912345678")
        @Size(max = 15) String phone,

        @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
        @Size(max = 2048) String avatarUrl,

        @Schema(description = "Vai trò trong công ty", example = "HR Manager")
        @Size(max = 50) String roleInCompany,

        @Schema(description = "Tên công ty", example = "Công ty TNHH ABC")
        @Size(max = 100) String companyName,

        @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
        String address,

        @Schema(description = "Mô tả công ty")
        String description,

        @Schema(description = "Website công ty", example = "https://company.com")
        @Size(max = 255) String website,

        @Schema(description = "Email công ty", example = "contact@company.com")
        @Email @Size(max = 100) String companyEmail,

        @Schema(description = "Số điện thoại công ty", example = "02812345678")
        @Size(max = 20) String companyPhone,

        @Schema(description = "URL logo công ty", example = "https://example.com/logo.png")
        String logoUrl,

        @Schema(description = "Mã số thuế", example = "0123456789")
        @Size(max = 20) String taxCode
) {
}
