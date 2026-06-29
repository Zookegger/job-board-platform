package com.yoedu.job_board_platform.dtos.notification;

import java.util.UUID;

public record NotificationRequest(UUID id, String status, String message) {
}
