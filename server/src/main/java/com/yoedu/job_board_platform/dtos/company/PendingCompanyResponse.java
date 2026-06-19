package com.yoedu.job_board_platform.dtos.company;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin công ty đang chờ phê duyệt.
 * Được sử dụng để trả về danh sách công ty PENDING cho Admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingCompanyResponse {
    private UUID id;
    private String companyName;
    private String email;
    private String phone;
    private String taxCode;
    private String address;
    private String description;
    private String website;
    private String logoUrl;
    private OffsetDateTime createdAt;
}
