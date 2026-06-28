package com.yoedu.job_board_platform.dtos.admin;

import java.time.LocalDate;
import java.util.List;

import com.yoedu.job_board_platform.models.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminApplicationChartResponse(
        @Schema(description = "Khoảng thời gian thống kê, ví dụ: 7 hoặc 30 ngày")
        int days,

        @Schema(description = "Ngày bắt đầu thống kê")
        LocalDate fromDate,

        @Schema(description = "Ngày kết thúc thống kê")
        LocalDate toDate,

        @Schema(description = "Tổng số đơn ứng tuyển trong khoảng thời gian")
        long totalApplications,

        @Schema(description = "Dữ liệu số đơn ứng tuyển theo từng ngày, dùng cho biểu đồ đường")
        List<DailyApplicationPoint> dailyApplications,

        @Schema(description = "Dữ liệu phân phối trạng thái ứng tuyển, dùng cho biểu đồ tròn/donut")
        List<StatusDistributionPoint> statusDistribution
) {
    public record DailyApplicationPoint(
            @Schema(description = "Ngày ứng tuyển")
            LocalDate date,

            @Schema(description = "Số đơn ứng tuyển trong ngày")
            long total
    ) {
    }

    public record StatusDistributionPoint(
            @Schema(description = "Trạng thái ứng tuyển")
            ApplicationStatus status,

            @Schema(description = "Số lượng đơn ứng tuyển theo trạng thái")
            long total,

            @Schema(description = "Tỷ lệ phần trăm của trạng thái so với tổng số đơn")
            double percentage
    ) {
    }
}