package com.yoedu.job_board_platform.dtos.admin;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminDashboardStatsResponse(
        @Schema(description = "Tổng số người dùng")
        long totalUsers,

        @Schema(description = "Tổng số công ty")
        long totalCompanies,

        @Schema(description = "Tổng số tin tuyển dụng đã duyệt")
        long totalJobs,

        @Schema(description = "Tổng số hồ sơ ứng tuyển")
        long totalApplications,

        @Schema(description = "Số người dùng mới trong 7 ngày gần nhất")
        long newUsers,

        @Schema(description = "Số tin tuyển dụng đang chờ duyệt")
        long pendingJobs,

        @Schema(description = "Số công ty đang chờ duyệt")
        long pendingCompanies
) {
}