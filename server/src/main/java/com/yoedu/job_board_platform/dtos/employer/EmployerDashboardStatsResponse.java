package com.yoedu.job_board_platform.dtos.employer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thống kê dashboard cho nhà tuyển dụng")
public record EmployerDashboardStatsResponse(
        @Schema(description = "Số tin tuyển dụng đang hoạt động") long activeJobs,
        @Schema(description = "Số tin tuyển dụng chờ duyệt") long pendingApprovalJobs,
        @Schema(description = "Số tin tuyển dụng nháp") long draftJobs,
        @Schema(description = "Số tin tuyển dụng đã hết hạn") long expiredJobs,
        @Schema(description = "Số tin tuyển dụng bị từ chối") long rejectedJobs,
        @Schema(description = "Tổng số hồ sơ ứng tuyển") long totalApplications,
        @Schema(description = "Số hồ sơ ứng tuyển mới trong 7 ngày") long newApplicationsThisWeek,
        @Schema(description = "Số hồ sơ đang chờ xử lý") long pendingApplications,
        @Schema(description = "Số hồ sơ đang xem xét") long reviewingApplications,
        @Schema(description = "Số hồ sơ đang phỏng vấn") long interviewApplications,
        @Schema(description = "Số hồ sơ đã tuyển") long hiredApplications
) {
}
