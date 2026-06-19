package com.yoedu.job_board_platform.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service để gửi thông báo tới người dùng.
 * Xử lý thông báo về thay đổi trạng thái công ty.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final CompanyRepository companyRepository;
    private final CompanyEmployerDetailRepository companyEmployerDetailRepository;
    private final UserRepository userRepository;

    /**
     * Gửi thông báo khi trạng thái công ty thay đổi.
     * Thông báo được gửi tới HR (người sáng lập/quản lý) của công ty đó.
     */
    @Override
    public void notifyCompanyStatusChange(UUID companyId, String status, String reason) {
        log.info("Sending notification for company {} with status: {}", companyId, status);
        
        try {
            // 1. Lấy thông tin công ty
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId));

            // 2. Lấy danh sách HR của công ty
            var employerDetails = companyEmployerDetailRepository.findAllByCompanyId(companyId);
            
            if (employerDetails.isEmpty()) {
                log.warn("Không tìm thấy HR nào cho công ty {}", companyId);
                return;
            }

            // 3. Tạo thông báo cho từng HR
            for (CompanyEmployerDetail employerDetail : employerDetails) {
                User employer = employerDetail.getProfile().getUser();
                
                String message = buildNotificationMessage(company.getCompanyName(), status, reason);
                
                Notification notification = Notification.builder()
                        .user(employer)
                        .type(NotificationStatus.COMPANY_STATUS_CHANGED)
                        .entityId(companyId)
                        .message(message)
                        .build();
                
                notificationRepository.save(notification);
                log.info("Created notification for user {} about company {}", employer.getId(), companyId);
            }
        } catch (Exception e) {
            log.error("Error sending notification for company {}: {}", companyId, e.getMessage(), e);
            // Không ném exception, chỉ log lỗi để không ảnh hưởng đến flow chính
        }
    }

    private String buildNotificationMessage(String companyName, String status, String reason) {
        return switch (status) {
            case "APPROVED" -> String.format("Công ty '%s' của bạn đã được phê duyệt.", companyName);
            case "REJECTED" -> String.format("Công ty '%s' của bạn đã bị từ chối. Lý do: %s", companyName, reason);
            case "SUSPENDED" -> String.format("Công ty '%s' của bạn đã bị tạm ngưng. Lý do: %s", companyName, reason);
            default -> String.format("Trạng thái công ty '%s' đã thay đổi", companyName);
        };
    }
}
