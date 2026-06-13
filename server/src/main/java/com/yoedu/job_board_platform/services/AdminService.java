package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;

/**
 * Service quản trị hệ thống.
 * Xử lý phê duyệt và từ chối công ty đăng ký.
 */
public interface AdminService {

    /**
     * Phê duyệt công ty đã đăng ký.
     * Chuyển trạng thái công ty từ PENDING sang APPROVED.
     *
     * @param companyId ID của công ty cần duyệt
     * @throws ResourceNotFoundException nếu không tìm thấy công ty
     */
    void approveCompany(UUID companyId);

    /**
     * Từ chối phê duyệt công ty.
     * Chuyển trạng thái công ty từ PENDING sang REJECTED kèm lý do.
     *
     * @param companyId ID của công ty cần từ chối
     * @throws ResourceNotFoundException nếu không tìm thấy công ty
     */
    void rejectCompany(UUID companyId);
}
