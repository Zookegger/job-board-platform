package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;

public interface AdminService {
    /**
     * Lấy danh sách công ty đang chờ duyệt với phân trang, tìm kiếm, lọc và sắp xếp.
     *
     * @param page     số trang bắt đầu từ 0
     * @param size     số phần tử trên mỗi trang
     * @param keyword  từ khóa tìm theo tên, email, điện thoại, MST, địa chỉ hoặc website
     * @param hasTaxCode true để chỉ lấy công ty có MST, false để lấy công ty thiếu MST
     * @param hasContact true để chỉ lấy công ty có thông tin liên hệ, false để lấy công ty thiếu liên hệ
     * @param sortBy   trường sắp xếp
     * @param direction hướng sắp xếp (asc/desc)
     * @return trang dữ liệu công ty pending
     */
    Page<PendingCompanyResponse> getPendingCompanies(
            int page,
            int size,
            String keyword,
            Boolean hasTaxCode,
            Boolean hasContact,
            String sortBy,
            String direction);

    /**
     * Duyệt một công ty đã đăng ký.
     *
     * @param companyId ID công ty cần duyệt
     * @throws ResourceNotFoundException when the company does not exist
     */
    void approveCompany(UUID companyId);

    /**
     * Từ chối một công ty đã đăng ký với lý do do admin cung cấp.
     *
     * @param companyId ID công ty cần từ chối
     * @param reason lý do từ chối
     * @throws ResourceNotFoundException when the company does not exist
     */
    void rejectCompany(UUID companyId, String reason);

    /**
     * Tạm ngưng hoạt động của công ty kèm lý do.
     *
     * @param companyId ID công ty cần tạm ngưng
     * @param reason lý do tạm ngưng
     * @throws ResourceNotFoundException when the company does not exist
     */
    void suspendCompany(UUID companyId, String reason);
}
