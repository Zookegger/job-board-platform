package com.yoedu.job_board_platform.dtos.company;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Thông tin trạng thái phê duyệt của công ty trả về cho employer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanyStatusResponse(
        UUID companyId,
        String name,
        String taxCode,
        /** Tên enum dạng String: PENDING | APPROVED | REJECTED | SUSPENDED */
        String approvalStatus,
        /** ISO 8601 */
        String submittedAt,
        UUID reviewedBy,
        String reviewNote,
        /** ISO 8601, null nếu chưa duyệt */
        String reviewedAt
) {}
