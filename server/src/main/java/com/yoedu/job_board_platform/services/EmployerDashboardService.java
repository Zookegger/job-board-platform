package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.dtos.employer.EmployerDashboardStatsResponse;

public interface EmployerDashboardService {

    /**
     * Lấy các số liệu thống kê tổng quan cho bảng điều khiển của nhà tuyển dụng.
     * Bao gồm số tin tuyển dụng theo trạng thái, tổng số hồ sơ ứng tuyển,
     * hồ sơ mới trong 7 ngày và phân bổ trạng thái hồ sơ.
     *
     * @param employerId ID của nhà tuyển dụng đang đăng nhập
     * @return đối tượng chứa dữ liệu thống kê tổng hợp cho employer dashboard
     */
    EmployerDashboardStatsResponse getStats(UUID employerId);
}
