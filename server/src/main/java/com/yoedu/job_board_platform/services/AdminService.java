package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.company.PendingCompanyResponse;

/**
 * Service quản trị hệ thống.
 * Xử lý phê duyệt, từ chối và tạm ngưng công ty đăng ký.
 */
public interface AdminService {

    /**
     * Lấy danh sách các công ty đang chờ duyệt.
     *
     * @param pageable thông tin phân trang
     * @return danh sách các công ty PENDING
     */
    Page<PendingCompanyResponse> getPendingCompanies(Pageable pageable);

    /**
     * Phê duyệt công ty đã đăng ký.
     * Chuyển trạng thái công ty từ PENDING sang APPROVED.
     * Tạo thông báo cho công ty thông báo việc phê duyệt.
     *
     * @param companyId ID của công ty cần duyệt
     * @throws ResourceNotFoundException nếu không tìm thấy công ty
     */
    void approveCompany(UUID companyId);

    /**
     * Từ chối phê duyệt công ty.
     * Chuyển trạng thái công ty từ PENDING sang REJECTED kèm lý do.
     * Tạo thông báo cho công ty thông báo việc từ chối.
     *
     * @param companyId ID của công ty cần từ chối
     * @param reason lý do từ chối
     * @throws ResourceNotFoundException nếu không tìm thấy công ty
     */
    void rejectCompany(UUID companyId, String reason);

    /**
     * Tạm ngưng hoạt động công ty.
     * Chuyển trạng thái công ty sang SUSPENDED kèm lý do.
     * Tạo thông báo cho công ty thông báo việc tạm ngưng.
     *
     * @param companyId ID của công ty cần tạm ngưng
     * @param reason lý do tạm ngưng
     * @throws ResourceNotFoundException nếu không tìm thấy công ty
     */
    void suspendCompany(UUID companyId, String reason);
}
