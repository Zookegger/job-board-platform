package com.yoedu.job_board_platform.dtos.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoedu.job_board_platform.models.CompanyStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCompanyListResponse(
        @Schema(description = "ID công ty") UUID id,
        @Schema(description = "Tên công ty") String companyName,
        @Schema(description = "Slug") String slug,
        @Schema(description = "Logo URL") String logoUrl,
        @Schema(description = "Email") String email,
        @Schema(description = "Số điện thoại") String phone,
        @Schema(description = "Địa chỉ") String address,
        @Schema(description = "Website") String website,
        @Schema(description = "Mã số thuế") String taxCode,
        @Schema(description = "Trạng thái") CompanyStatus status,
        @JsonProperty("isApproved") @Schema(description = "Đã duyệt?") boolean isApproved,
        @Schema(description = "Lý do từ chối") String rejectionReason,
        @Schema(description = "Lý do tạm ngưng") String suspensionReason,
        @Schema(description = "Ngày tạo") OffsetDateTime createdAt,
        @Schema(description = "Ngày duyệt") OffsetDateTime approvedAt
) {
}
