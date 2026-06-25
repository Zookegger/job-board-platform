package com.yoedu.job_board_platform.dtos.admin;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminDashboardStatsResponse(
        @Schema(description = "Tổng số người dùng")
        long totalUsers,

        @Schema(description = "Tổng số công ty")
        long totalCompanies,

        @Schema(description = "Tổng số tin tuyển dụng")
        long totalJobs,

        @Schema(description = "Tổng số hồ sơ ứng tuyển")
        long totalApplications
) {
}