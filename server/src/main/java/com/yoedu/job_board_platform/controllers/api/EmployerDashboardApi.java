package com.yoedu.job_board_platform.controllers.api;

import com.yoedu.job_board_platform.dtos.employer.EmployerDashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Employer — Dashboard", description = "Thống kê dashboard cho nhà tuyển dụng. Yêu cầu role EMPLOYER.")
public interface EmployerDashboardApi {

    @Operation(summary = "Dashboard thống kê tổng quan", description = """
            Lấy các chỉ số tổng quan cho màn hình Employer Dashboard:
            số tin tuyển dụng theo trạng thái, tổng hồ sơ ứng tuyển,
            hồ sơ mới trong tuần và phân bổ trạng thái hồ sơ.
            """)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dữ liệu thống kê tổng quan dashboard",
            content = @Content)
    ResponseEntity<EmployerDashboardStatsResponse> getDashboardStats();
}
