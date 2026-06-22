package com.yoedu.job_board_platform.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.yoedu.job_board_platform.dtos.report.CreateReportRequest;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * API báo cáo vi phạm dành cho người dùng đã xác thực.
 * Cho phép CANDIDATE, EMPLOYER, ADMIN gửi báo cáo về bài tuyển dụng hoặc công ty.
 */
@Tag(name = "Reports — Báo cáo vi phạm", description = "API báo cáo vi phạm dành cho người dùng đã xác thực. Yêu cầu đăng nhập.")
public interface ReportApi {

    /**
     * Tạo báo cáo vi phạm mới.
     * <p>
     * Người dùng có thể báo cáo một bài tuyển dụng (jobId) hoặc một công ty (companyId),
     * nhưng không thể báo cáo cả hai cùng lúc. Lý do báo cáo là bắt buộc.
     * </p>
     *
     * @param request thông tin báo cáo
     * @return thông tin báo cáo đã tạo
     */
    @Operation(summary = "Tạo báo cáo vi phạm", description = """
            Gửi báo cáo vi phạm về một bài tuyển dụng hoặc công ty.
            Người dùng phải cung cấp đúng một trong hai: jobId (báo cáo bài tuyển dụng)
            hoặc companyId (báo cáo công ty). Lý do báo cáo bắt buộc phải có.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo báo cáo thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ: thiếu target hoặc cung cấp cả hai target", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài tuyển dụng/công ty", content = @Content)
    })
    ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request);
}
