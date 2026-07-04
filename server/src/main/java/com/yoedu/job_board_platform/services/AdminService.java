package com.yoedu.job_board_platform.services;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.dtos.admin.*;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.dtos.user.UserFullResponse;
import com.yoedu.job_board_platform.models.ReportReason;
import com.yoedu.job_board_platform.models.ReportStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {
	/**
	 * Lấy các số liệu thống kê tổng quan cho bảng điều khiển (dashboard) của quản
	 * trị viên.
	 * Bao gồm các chỉ số như tổng số người dùng, số lượng công việc, ứng viên, v.v.
	 *
	 * @return đối tượng chứa dữ liệu thống kê tổng hợp cho admin dashboard
	 */
	AdminDashboardStatsResponse getDashboardStats();

	/**
	 * Lấy dữ liệu thống kê số lượng hồ sơ ứng tuyển để vẽ biểu đồ,
	 * quét ngược về quá khứ dựa trên số ngày được chỉ định.
	 *
	 * @param days số ngày gần nhất cần truy xuất dữ liệu (ví dụ: 7, 30, 90 ngày)
	 *
	 * @return đối tượng chứa dữ liệu trục thời gian và số lượng để render biểu đồ
	 */
	AdminApplicationChartResponse getApplicationChartStats(int days);

	/**
	 * Lấy danh sách người dùng trong hệ thống với khả năng lọc động.
	 * Pageable sẽ tự xử lý page, size, sort từ request:
	 * /admin/users?role=EMPLOYER&isActive=true&page=0&size=10
	 *
	 * @param role     vai trò của người dùng (ADMIN, EMPLOYER, CANDIDATE), null: bỏ
	 *                 qua bộ lọc
	 * @param isActive true: tài khoản đang hoạt động, false: đã bị khóa, null: bỏ
	 *                 qua bộ lọc
	 * @param pageable phân trang và sắp xếp của Spring Data
	 *
	 * @return trang dữ liệu danh sách người dùng
	 */
	Page<User> getUsers(String keyword, UserRole role, Boolean isActive, Pageable pageable);

	/**
	 * Lấy thông tin chi tiết của một người dùng theo ID.
	 * Bao gồm thông tin cơ bản, profile, và các dữ liệu liên quan đến vai trò
	 * (kỹ năng, CV, lịch sử ứng tuyển cho CANDIDATE; thông tin công tin, tin
	 * tuyển dụng, hoạt động tuyển dụng cho EMPLOYER).
	 *
	 * @param userId ID người dùng cần xem chi tiết
	 *
	 * @return thông tin đầy đủ của người dùng
	 */
	UserFullResponse getUserDetail(UUID userId);

	/**
	 * Lấy danh sách công ty đang chờ duyệt.
	 * Pageable sẽ tự xử lý page, size, sort từ request:
	 * /admin/companies/pending?page=0&size=10&sort=createdAt,desc
	 *
	 * @param keyword    từ khóa tìm kiếm theo tên, email, điện thoại, MST, địa chỉ
	 *                   hoặc website
	 * @param hasTaxCode true: có MST, false: thiếu MST, null: bỏ qua
	 * @param hasContact true: có thông tin liên hệ, false: thiếu liên hệ, null: bỏ
	 *                   qua
	 * @param pageable   phân trang và sắp xếp của Spring Data
	 *
	 * @return trang dữ liệu công ty pending
	 */
	Page<PendingCompanyResponse> getPendingCompanies(
			String keyword,
			Boolean hasTaxCode,
			Boolean hasContact,
			Pageable pageable);

	/**
	 * Duyệt một công ty đã đăng ký.
	 *
	 * @param companyId ID công ty cần duyệt
	 */
	void approveCompany(UUID companyId);

	/**
	 * Từ chối một công ty đã đăng ký.
	 *
	 * @param companyId ID công ty cần từ chối
	 * @param request   lý do từ chối
	 */
	void rejectCompany(UUID companyId, CompanyRejectionRequest request);

	/**
	 * Tạm ngưng một công ty.
	 *
	 * @param companyId ID công ty cần tạm ngưng
	 * @param request   lý do tạm ngưng
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
	 *
	 * @param keyword  từ khóa tìm kiếm (tên, email, MST, địa chỉ)
	 * @param status   trạng thái công ty: PENDING, APPROVED, REJECTED, SUSPENDED
	 *                 (null: tất cả)
	 * @param pageable phân trang và sắp xếp của Spring Data
	 *
	 * @return trang dữ liệu công ty
	 */
	Page<AdminCompanyListResponse> getAllCompanies(String keyword, String status, Pageable pageable);

	/**
	 * Lấy danh sách tất cả tin tuyển dụng, có thể lọc theo trạng thái.
	 *
	 * @param status   trạng thái tin tuyển dụng: DRAFT, PENDING_APPROVAL, ACTIVE,
	 *                 EXPIRED, REJECTED (null: tất cả)
	 * @param pageable phân trang và sắp xếp của Spring Data
	 *
	 * @return trang dữ liệu tin tuyển dụng
	 */
	Page<AdminJobListResponse> getAllJobs(String status, Pageable pageable);

	/**
	 * Lấy danh sách tin tuyển dụng đang chờ duyệt (PENDING_APPROVAL).
	 *
	 * @param pageable phân trang và sắp xếp của Spring Data
	 *
	 * @return trang dữ liệu tin tuyển dụng pending
	 */
	Page<PendingJobResponse> getPendingJobs(Pageable pageable);

	/**
	 * Duyệt tin tuyển dụng — chuyển trạng thái từ PENDING_APPROVAL sang ACTIVE.
	 *
	 * @param jobId ID tin tuyển dụng cần duyệt
	 *
	 * @throws BadRequestException nếu tin tuyển dụng không ở trạng thái chờ duyệt
	 */
	void approveJob(UUID jobId);

	/**
	 * Từ chối tin tuyển dụng — chuyển trạng thái từ PENDING_APPROVAL sang REJECTED
	 * với lý do.
	 *
	 * @param jobId  ID tin tuyển dụng cần từ chối
	 * @param reason lý do từ chối (bắt buộc)
	 *
	 * @throws BadRequestException nếu tin tuyển dụng không ở trạng thái chờ duyệt
	 *                             hoặc thiếu lý do
	 */
	void rejectJob(UUID jobId, String reason);

	/**
	 * Lấy danh sách báo cáo vi phạm, có thể lọc theo trạng thái.
	 *
	 * @param status   lọc theo trạng thái: PENDING, REVIEWED, DISMISSED, RESOLVED
	 *                 (null: tất cả)
	 * @param pageable phân trang và sắp xếp
	 *
	 * @return trang dữ liệu báo cáo
	 */
	Page<ReportResponse> getReports(ReportStatus status, ReportReason reason, Pageable pageable);

	/**
	 * Xem xét báo cáo — chuyển trạng thái từ PENDING sang REVIEWED.
	 *
	 * @param reportId    ID báo cáo cần xem xét
	 * @param reviewNotes ghi chú xử lý (tuỳ chọn)
	 */
	void reviewReport(UUID reportId, String reviewNotes);

	/**
	 * Bác bỏ báo cáo — chuyển trạng thái từ PENDING/REVIEWED sang DISMISSED.
	 *
	 * @param reportId    ID báo cáo cần bác bỏ
	 * @param reviewNotes ghi chú xử lý (tuỳ chọn)
	 */
	void dismissReport(UUID reportId, String reviewNotes);

	/**
	 * Giải quyết báo cáo — chuyển trạng thái từ REVIEWED sang RESOLVED.
	 *
	 * @param reportId    ID báo cáo cần giải quyết
	 * @param reviewNotes ghi chú xử lý (tuỳ chọn)
	 */
	void resolveReport(UUID reportId, String reviewNotes);
}
