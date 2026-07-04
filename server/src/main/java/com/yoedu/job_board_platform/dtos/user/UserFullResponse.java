package com.yoedu.job_board_platform.dtos.user;

import com.yoedu.job_board_platform.models.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserFullResponse(
		@Schema(description = "ID người dùng")
		UUID id,

		@Schema(description = "Email đăng nhập")
		String email,

		@Schema(description = "Vai trò người dùng")
		UserRole role,

		@Schema(description = "Tài khoản có đang hoạt động")
		boolean isActive,

		@Schema(description = "Họ và tên")
		String fullName,

		@Schema(description = "Số điện thoại")
		String phone,

		@Schema(description = "URL ảnh đại diện")
		String avatarUrl,

		@Schema(description = "Thời điểm tạo tài khoản")
		OffsetDateTime createdAt,

		@Schema(description = "Thời điểm cập nhật lần cuối")
		OffsetDateTime updatedAt,

		@Schema(description = "Danh sách kỹ năng (chỉ có khi role = CANDIDATE)")
		List<CandidateSkillInfo> skills,

		@Schema(description = "Thông tin CV (chỉ có khi role = CANDIDATE)")
		ResumeInfo resume,

		@Schema(description = "Lịch sử ứng tuyển (chỉ có khi role = CANDIDATE)")
		List<ApplicationInfo> applications,

		@Schema(description = "Thông tin công ty (chỉ có khi role = EMPLOYER)")
		CompanyInfo company,

		@Schema(description = "Vai trò trong công ty (chỉ có khi role = EMPLOYER)")
		String roleInCompany,

		@Schema(description = "Danh sách tin tuyển dụng (chỉ có khi role = EMPLOYER)")
		List<JobPostingInfo> jobPostings,

		@Schema(description = "Tổng quan hoạt động tuyển dụng (chỉ có khi role = EMPLOYER)")
		HiringActivityInfo hiringActivity
) {
	public record CandidateSkillInfo(
			@Schema(description = "ID kỹ năng")
			Integer skillId,

			@Schema(description = "Tên kỹ năng")
			String skillName,

			@Schema(description = "Mức độ thành thạo")
			ProficientLevel proficientLevel
	) {
	}

	public record ResumeInfo(
			@Schema(description = "ID CV")
			UUID id,

			@Schema(description = "Tiêu đề CV")
			String title,

			@Schema(description = "Tên file gốc")
			String originalFileName,

			@Schema(description = "Kích thước file (bytes)")
			long fileSize,

			@Schema(description = "Loại file")
			String fileType,

			@Schema(description = "Thời điểm tải lên")
			OffsetDateTime createdAt
	) {
	}

	public record ApplicationInfo(
			@Schema(description = "ID đơn ứng tuyển")
			UUID id,

			@Schema(description = "ID tin tuyển dụng")
			UUID jobId,

			@Schema(description = "Tiêu đề công việc")
			String jobTitle,

			@Schema(description = "Tên công ty")
			String companyName,

			@Schema(description = "Trạng thái đơn ứng tuyển")
			ApplicationStatus status,

			@Schema(description = "Thời điểm ứng tuyển")
			OffsetDateTime appliedAt
	) {
	}

	public record CompanyInfo(
			@Schema(description = "ID công ty")
			UUID id,

			@Schema(description = "Tên công ty")
			String companyName,

			@Schema(description = "Slug")
			String slug,

			@Schema(description = "URL logo")
			String logoUrl,

			@Schema(description = "Email công ty")
			String email,

			@Schema(description = "Số điện thoại công ty")
			String phone,

			@Schema(description = "Địa chỉ")
			String address,

			@Schema(description = "Website")
			String website,

			@Schema(description = "Mã số thuế")
			String taxCode,

			@Schema(description = "Trạng thái công ty")
			CompanyStatus status,

			@Schema(description = "Đã được duyệt")
			boolean isApproved,

			@Schema(description = "Ngày tạo")
			OffsetDateTime createdAt,

			@Schema(description = "Ngày duyệt")
			OffsetDateTime approvedAt
	) {
	}

	public record JobPostingInfo(
			@Schema(description = "ID tin tuyển dụng")
			UUID id,

			@Schema(description = "Tiêu đề công việc")
			String title,

			@Schema(description = "Slug")
			String slug,

			@Schema(description = "Trạng thái tin tuyển dụng")
			JobStatus status,

			@Schema(description = "Tên ngành nghề")
			String categoryName,

			@Schema(description = "Địa điểm")
			String location,

			@Schema(description = "Thời điểm tạo")
			OffsetDateTime createdAt
	) {
	}

	public record HiringActivityInfo(
			@Schema(description = "Tổng số đơn ứng tuyển")
			long totalApplications,

			@Schema(description = "Số đơn đang chờ xử lý")
			long pendingApplications,

			@Schema(description = "Số đơn đang xem xét")
			long reviewingApplications,

			@Schema(description = "Số đơn phỏng vấn")
			long interviewApplications,

			@Schema(description = "Số đơn đã tuyển")
			long hiredApplications,

			@Schema(description = "Số đơn bị từ chối")
			long rejectedApplications
	) {
	}
}
