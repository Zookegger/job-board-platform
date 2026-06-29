package com.yoedu.job_board_platform.dtos.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoedu.job_board_platform.models.NotificationStatus;

public record NotificationResponse(
        UUID id,
        NotificationStatus type,
        UUID entityId,
        String message,
        @JsonProperty("isRead") boolean isRead,
        OffsetDateTime createdAt,
        OffsetDateTime readAt) {
}
