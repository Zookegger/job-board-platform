package com.yoedu.job_board_platform.controllers;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.NotificationApi;
import com.yoedu.job_board_platform.dtos.notification.NotificationResponse;
import com.yoedu.job_board_platform.mappers.NotificationMapper;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.NotificationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE + "/notifications")
@PreAuthorize(AuthorizationConstants.AUTHENTICATED)
public class NotificationController implements NotificationApi {
	private final NotificationService notificationService;
	private final NotificationMapper notificationMapper;
	private final SecurityUtil securityUtil;

	@Override
	public ResponseEntity<Page<NotificationResponse>> getNotifications(Pageable pageable) {
		Page<Notification> notifications = notificationService.getNotifications(securityUtil.getCurrentUserId(), pageable);

		return ResponseEntity.ok(notifications.map(notificationMapper::toResponse));
	}

	@Override
	public ResponseEntity<Long> getUnreadCount() {
		return ResponseEntity.ok(notificationService.getUnreadCount(securityUtil.getCurrentUserId()));
	}

	@Override
	public ResponseEntity<Void> markAsRead(UUID id) {
		notificationService.markAsRead(securityUtil.getCurrentUserId(), id);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Void> markAllAsRead() {
		notificationService.markAllAsRead(securityUtil.getCurrentUserId());
		return ResponseEntity.noContent().build();
	}
}
