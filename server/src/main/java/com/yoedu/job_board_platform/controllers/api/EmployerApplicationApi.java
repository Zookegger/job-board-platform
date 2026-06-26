package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.application.EmployerApplicationListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Nhà tuyển dụng — Quản lý ứng viên", description = "Xem danh sách, chi tiết và cập nhật trạng thái đơn ứng tuyển. Yêu cầu role EMPLOYER.")
public interface EmployerApplicationApi {

    @Operation(summary = "Danh sách ứng viên", description = "Lấy danh sách đơn ứng tuyển vào các tin của công ty mình.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách đơn ứng tuyển (có phân trang)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content)
    })
    ResponseEntity<Page<EmployerApplicationListResponse>> getApplications(
            @Parameter(description = "Lọc theo ID tin tuyển dụng") UUID jobId,
            @Parameter(description = "Lọc theo trạng thái") String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") int page,
            @Parameter(description = "Số phần tử mỗi trang") int size);

    @Operation(summary = "Chi tiết đơn ứng tuyển")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chi tiết đơn ứng tuyển", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem đơn này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    ResponseEntity<?> getApplicationDetail(
            @Parameter(description = "ID của đơn ứng tuyển", required = true) UUID id);

    @Operation(summary = "Cập nhật trạng thái ứng viên", description = "Thay đổi trạng thái: REVIEWING, INTERVIEW, HIRED, REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật đơn này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    ResponseEntity<?> updateApplicationStatus(
            @Parameter(description = "ID của đơn ứng tuyển", required = true) UUID id,
            @Parameter(description = "Trạng thái mới: REVIEWING, INTERVIEW, HIRED, REJECTED", required = true) String status,
            @Parameter(description = "Lý do thay đổi (tuỳ chọn)") String reason);
}
