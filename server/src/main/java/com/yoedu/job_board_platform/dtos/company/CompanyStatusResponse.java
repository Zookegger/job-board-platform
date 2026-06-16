package com.yoedu.job_board_platform.dtos.company;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yoedu.job_board_platform.models.CompanyStatus;

/**
 * Thông tin trạng thái phê duyệt của công ty trả về cho employer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanyStatusResponse(
        UUID companyId,
        String name,
        String taxCode,
        CompanyStatus approvalStatus,
        OffsetDateTime submittedAt,
        String reviewNote,
        OffsetDateTime reviewedAt
) {}
