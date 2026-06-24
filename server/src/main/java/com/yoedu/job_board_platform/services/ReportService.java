package com.yoedu.job_board_platform.services;

import com.yoedu.job_board_platform.dtos.report.CreateReportRequest;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;

/**
 * Service xử lý nghiệp vụ báo cáo vi phạm.
 */
public interface ReportService {

    /**
     * Tạo báo cáo vi phạm mới.
     * <p>
     * Kiểm tra dữ liệu đầu vào: phải cung cấp đúng một target (job XOR company).
     * Nếu target là bài tuyển dụng, kiểm tra bài tuyển dụng tồn tại.
     * Nếu target là công ty, kiểm tra công ty tồn tại.
     * Lưu báo cáo và trả về thông tin báo cáo đã tạo.
     * </p>
     *
     * @param request thông tin báo cáo
     * @return thông tin báo cáo đã tạo
     */
    ReportResponse createReport(CreateReportRequest request);
}
