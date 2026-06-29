package com.yoedu.job_board_platform.controllers.api;

import com.yoedu.job_board_platform.dtos.notification.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@Tag(name = "Notifications")
@SecurityRequirement(name = "Bearer")
public interface NotificationApi {
	@GetMapping
	@Operation(summary = "Get paginated notifications for current user")
	ResponseEntity<Page<NotificationResponse>> getNotifications(Pageable pageable);

	@GetMapping("/unread-count")
	@Operation(summary = "Get unread notification count")
	ResponseEntity<Long> getUnreadCount();

	@PutMapping("/{id}/read")
	@Operation(summary = "Mark a notification as read")
	ResponseEntity<Void> markAsRead(@PathVariable UUID id);

	@PutMapping("/read-all")
	@Operation(summary = "Mark all notifications as read")
	ResponseEntity<Void> markAllAsRead();
}
