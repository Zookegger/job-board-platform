package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.BASE + "/employer/applications")
@PreAuthorize("hasRole('EMPLOYER')")
@Tag(name = "Nhà tuyển dụng — Quản lý ứng viên", description = "Xem danh sách, chi tiết và cập nhật trạng thái đơn ứng tuyển. Yêu cầu role EMPLOYER.")
public class EmployerApplicationController {

    @GetMapping
    @Operation(summary = "Danh sách ứng viên", description = """
            Lấy danh sách đơn ứng tuyển vào các tin của công ty mình.
            Có thể lọc theo job_id và/hoặc trạng thái.
            Kết quả phân trang, mỗi item bao gồm thông tin ứng viên và trạng thái đơn.
            Dùng cho màn hình CandidateTable.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sách đơn ứng tuyển (có phân trang)", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content)
    })
    public ResponseEntity<?> getApplications(
            @Parameter(description = "Lọc theo ID tin tuyển dụng", example = "1")
            @RequestParam(required = false) Long jobId,
            @Parameter(description = "Lọc theo trạng thái: PENDING, REVIEWING, INTERVIEW, HIRED, REJECTED", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách ứng viên");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn ứng tuyển", description = """
            Xem chi tiết đơn ứng tuyển của ứng viên: CV, thư giới thiệu, thông tin cá nhân,
            trạng thái hiện tại và lịch sử thay đổi trạng thái.
            Chỉ xem được đơn thuộc về tin của công ty mình.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chi tiết đơn ứng tuyển + thông tin ứng viên", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền xem đơn này", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    public ResponseEntity<?> getApplicationDetail(
            @Parameter(description = "ID của đơn ứng tuyển", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết hồ sơ");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái ứng viên", description = """
            Thay đổi trạng thái của đơn ứng tuyển theo quy trình tuyển dụng:
            PENDING → REVIEWING → INTERVIEW → HIRED | REJECTED.
            Có thể chuyển từ bất kỳ trạng thái nào sang REJECTED.
            Nếu reject, có thể kèm lý do (gửi email thông báo cho ứng viên).
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ hoặc chuyển trạng thái không được phép", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật đơn này", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    public ResponseEntity<?> updateApplicationStatus(
            @Parameter(description = "ID của đơn ứng tuyển", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Trạng thái mới: REVIEWING, INTERVIEW, HIRED, REJECTED", example = "INTERVIEW", required = true)
            @RequestParam String status,
            @Parameter(description = "Lý do từ chối (bắt buộc nếu status = REJECTED)", example = "Không đáp ứng yêu cầu chuyên môn")
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
