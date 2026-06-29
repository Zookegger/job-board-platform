package com.yoedu.job_board_platform.controllers;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.dtos.notification.NotificationResponse;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SecurityUtil securityUtil;

    /** Danh sách thông báo của user hiện tại, sắp xếp mới nhất trước. */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @ParameterObject @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
        UUID userId = securityUtil.getCurrentUserId();
        Page<NotificationResponse> page = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(page);
    }

    /** Số lượng thông báo chưa đọc của user hiện tại. */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        long count = notificationRepository.countByUserIdAndReadAtIsNull(
                securityUtil.getCurrentUserId());
        return ResponseEntity.ok(count);
    }

    /** Đánh dấu một thông báo là đã đọc. */
    @PatchMapping("/{id}/read")
    @Transactional
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        UUID userId = securityUtil.getCurrentUserId();

        // 1. Notification không tồn tại → 404
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));

        // 2. Notification tồn tại nhưng không thuộc về user hiện tại → 403
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền đọc thông báo này");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(OffsetDateTime.now());
            notificationRepository.save(notification);
        }
        return ResponseEntity.noContent().build();
    }

    /** Đánh dấu tất cả thông báo chưa đọc là đã đọc. */
    @PatchMapping("/read-all")
    @Transactional
    public ResponseEntity<Void> markAllAsRead() {
        notificationRepository.markAllAsReadByUserId(
                securityUtil.getCurrentUserId(), OffsetDateTime.now());
        return ResponseEntity.noContent().build();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getEntityId(),
                n.getMessage(),
                n.getReadAt() != null,
                n.getCreatedAt(),
                n.getReadAt());
    }
}
