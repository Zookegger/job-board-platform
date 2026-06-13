package com.yoedu.job_board_platform.dtos.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @Schema(description = "Tên công ty", example = "Yoedu Technology Corporation")
        @Size(max = 100) String companyName,

        @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
        String address,

        @Schema(description = "Mô tả công ty")
        String description,

        @Schema(description = "Website công ty", example = "https://company.com")
        @Size(max = 255) String website,

        @Schema(description = "URL logo công ty", example = "https://example.com/logo.png")
        String logoUrl,

        @Schema(description = "Email công ty", example = "contact@yoedu.com")
        @Size(max = 255) String email,

        @Schema(description = "Số điện thoại công ty", example = "02812345678")
        @Size(max = 15) String phone,

        @Schema(description = "Mã số thuế", example = "0123456789")
        @Size(max = 20) String taxCode
) {
}
