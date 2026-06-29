package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
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

    /**
     * Trả về số lượng thông báo chưa đọc của user hiện tại.
     * userId được bóc tách từ JWT — không nhận qua param/body.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        long count = notificationRepository.countByUserIdAndReadAtIsNull(
                securityUtil.getCurrentUserId());
        return ResponseEntity.ok(count);
    }
}
