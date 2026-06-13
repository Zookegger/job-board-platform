package com.yoedu.job_board_platform.dtos.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.CompanyStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record PendingCompanyResponse(
        @Schema(description = "ID cong ty", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Ten cong ty", example = "Yoedu Technology Corporation")
        String companyName,

        @Schema(description = "Slug", example = "yoedu-technology-corporation")
        String slug,

        @Schema(description = "Dia chi", example = "123 Nguyen Hue, Quan 1, TP.HCM")
        String address,

        @Schema(description = "Mo ta cong ty")
        String description,

        @Schema(description = "Website", example = "https://company.com")
        String website,

        @Schema(description = "URL logo", example = "https://example.com/logo.png")
        String logoUrl,

        @Schema(description = "Email cong ty", example = "contact@yoedu.com")
        String email,

        @Schema(description = "So dien thoai cong ty", example = "02812345678")
        String phone,

        @Schema(description = "Ma so thue", example = "0123456789")
        String taxCode,

        @Schema(description = "Trang thai duyet", example = "PENDING")
        CompanyStatus status,

        @Schema(description = "Da duoc duyet?", example = "false")
        boolean isApproved,

        @Schema(description = "Ngay dang ky", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime createdAt,

        @Schema(description = "Ngay duyet", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime approvedAt,

        @Schema(description = "Ten nguoi phu trach", example = "Nguyen Van A")
        String employerName,

        @Schema(description = "Email tai khoan nha tuyen dung", example = "hr@company.com")
        String employerEmail,

        @Schema(description = "So dien thoai nguoi phu trach", example = "0901234567")
        String employerPhone,

        @Schema(description = "Vai tro trong cong ty", example = "HR")
        String roleInCompany
) {
}
