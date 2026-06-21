package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.dtos.admin.AdminCompanyListResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminJobListResponse;
import com.yoedu.job_board_platform.dtos.admin.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.admin.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingJobResponse;

public interface AdminService {

    /**
     * Lấy danh sách công ty đang chờ duyệt.
     *
     * Pageable sẽ tự xử lý page, size, sort từ request:
     * /admin/companies/pending?page=0&size=10&sort=createdAt,desc
     *
     * @param keyword từ khóa tìm kiếm theo tên, email, điện thoại, MST, địa chỉ hoặc website
     * @param hasTaxCode true: có MST, false: thiếu MST, null: bỏ qua
     * @param hasContact true: có thông tin liên hệ, false: thiếu liên hệ, null: bỏ qua
     * @param pageable phân trang và sắp xếp của Spring Data
     * @return trang dữ liệu công ty pending
     */
    Page<PendingCompanyResponse> getPendingCompanies(
            String keyword,
            Boolean hasTaxCode,
            Boolean hasContact,
            Pageable pageable
    );

    /**
     * Duyệt một công ty đã đăng ký.
     *
     * @param companyId ID công ty cần duyệt
     * @param request lý do phê duyệt
     */
    void approveCompany(UUID companyId);

    /**
     * Từ chối một công ty đã đăng ký.
     *
     * @param companyId ID công ty cần từ chối
     * @param request lý do từ chối
     */
    void rejectCompany(UUID companyId, CompanyRejectionRequest request);

    /**
     * Tạm ngưng một công ty.
     *
     * @param companyId ID công ty cần tạm ngưng
     * @param request lý do tạm ngưng
     */
    void suspendCompany(UUID companyId, CompanySuspensionRequest request);

    /**
     * Mở tạm ngưng một công ty — khôi phục từ SUSPENDED về APPROVED.
     *
     * @param companyId ID công ty cần mở tạm ngưng
     */
    void unsuspendCompany(UUID companyId);

    /**
     * Lấy danh sách tất cả công ty, có thể lọc theo trạng thái.
     */
    Page<AdminCompanyListResponse> getAllCompanies(String keyword, String status, Pageable pageable);

    /**
     * Lấy danh sách tất cả tin tuyển dụng, có thể lọc theo trạng thái.
     */
    Page<AdminJobListResponse> getAllJobs(String status, Pageable pageable);

    /**
     * Lấy danh sách tin tuyển dụng đang chờ duyệt (PENDING_APPROVAL).
     */
    Page<PendingJobResponse> getPendingJobs(Pageable pageable);

    /**
     * Duyệt tin tuyển dụng — chuyển trạng thái sang ACTIVE.
     */
    void approveJob(UUID jobId);

    /**
     * Từ chối tin tuyển dụng — chuyển trạng thái sang REJECTED với lý do.
     */
    void rejectJob(UUID jobId, String reason);
}
