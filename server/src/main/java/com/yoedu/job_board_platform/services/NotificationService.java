package com.yoedu.job_board_platform.services;

import java.util.UUID;

/**
 * Service để gửi thông báo tới người dùng.
 */
public interface NotificationService {

    /**
     * Gửi thông báo khi trạng thái công ty thay đổi.
     * Thông báo được gửi tới HR (người sáng lập/quản lý) của công ty đó.
     *
     * @param companyId ID của công ty
     * @param status trạng thái mới (APPROVED, REJECTED, SUSPENDED)
     * @param reason lý do thay đổi (nếu có)
     */
    void notifyCompanyStatusChange(UUID companyId, String status, String reason);
}
