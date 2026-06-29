package com.yoedu.job_board_platform.dtos.notification;

import com.yoedu.job_board_platform.models.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationResponse(
		@Schema(description = "ID duy nhất của thông báo", example = "123e4567-e89b-12d3-a456-426614174000")
		UUID id,

		@Schema(description = "Loại thông báo (ví dụ: SYSTEM, TRANSACTION, ALERT)", example = "SYSTEM")
		NotificationStatus type,

		@Schema(description = "ID của đối tượng liên quan đến thông báo này (nếu có)", example = "987f6543-e21b-34d5-c678-987654321000")
		UUID entityId,

		@Schema(description = "Nội dung chi tiết của thông báo", example = "Tài khoản của bạn vừa đăng nhập từ một thiết bị mới.")
		String message,

		@Schema(description = "Thời gian tạo thông báo (ISO-8601)", example = "2026-06-29T12:00:00+07:00")
		OffsetDateTime createdAt,

		@Schema(description = "Thời gian người dùng đọc thông báo (null nếu chưa đọc)", example = "2026-06-29T12:05:00+07:00")
		OffsetDateTime readAt,

		@Schema(description = "Trạng thái đã đọc (được tính toán từ readAt)", example = "true")
		@JsonProperty("isRead")
    boolean isRead
) {
}
