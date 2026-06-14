package com.yoedu.job_board_platform.dtos.company;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Một bản ghi trong lịch sử phê duyệt công ty.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApprovalLogResponse(
        UUID actorId,
        String oldStatus,
        String newStatus,
        String note,
        /** ISO 8601 */
        String createdAt
) {}
