package com.yoedu.job_board_platform.dtos.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoedu.job_board_platform.models.CompanyStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record PendingCompanyResponse(
        @Schema(description = "ID công ty", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Tên công ty", example = "Yoedu Technology Corporation")
        String companyName,

        @Schema(description = "Slug", example = "yoedu-technology-corporation")
        String slug,

        @Schema(description = "Địa chỉ", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
        String address,

        @Schema(description = "Mô tả công ty")
        String description,

        @Schema(description = "Website", example = "https://company.com")
        String website,

        @Schema(description = "URL logo", example = "https://example.com/logo.png")
        String logoUrl,

        @Schema(description = "Email công ty", example = "contact@yoedu.com")
        String email,

        @Schema(description = "Số điện thoại công ty", example = "02812345678")
        String phone,

        @Schema(description = "Mã số thuế", example = "0123456789")
        String taxCode,

        @Schema(description = "Trạng thái duyệt", example = "PENDING")
        CompanyStatus status,

        @JsonProperty("isApproved")
        @Schema(description = "Đã được duyệt?", example = "false")
        boolean isApproved,

        @Schema(description = "Ngày đăng ký", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime createdAt,

        @Schema(description = "Ngày duyệt", example = "2026-06-13T10:30:00+07:00")
        OffsetDateTime approvedAt,

        @Schema(description = "Tên người phụ trách", example = "Nguyễn Văn A")
        String employerName,

        @Schema(description = "Email tài khoản nhà tuyển dụng", example = "hr@company.com")
        String employerEmail,

        @Schema(description = "Số điện thoại người phụ trách", example = "0901234567")
        String employerPhone,

        @Schema(description = "Vai trò trong công ty", example = "HR")
        String roleInCompany
) {
}
