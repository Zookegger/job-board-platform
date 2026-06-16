package com.yoedu.job_board_platform.dtos.company;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yoedu.job_board_platform.models.CompanyStatus;

/**
 * Một bản ghi trong lịch sử phê duyệt công ty.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApprovalLogResponse(
        UUID actorId,
        CompanyStatus oldStatus,
        CompanyStatus newStatus,
        String note,
        OffsetDateTime createdAt
) {}
