package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Ứng tuyển & Timeline", description = "Nộp đơn, xem danh sách, chi tiết, lịch sử trạng thái, rút đơn. Yêu cầu role CANDIDATE.")
public interface ApplicationApi {

    @Operation(summary = "Nộp đơn ứng tuyển", description = """
            Ứng tuyển vào một công việc.
            Yêu cầu job_id và có thể kèm cover letter.
            Mỗi ứng viên chỉ được nộp một đơn cho một công việc (unique constraint trên candidate_id + job_id).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nộp đơn thành công — đơn ở trạng thái PENDING", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc đã nộp đơn cho job này rồi", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ CANDIDATE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy công việc", content = @Content)
    })
    ResponseEntity<?> submitApplication(ApplicationRequest request);

    @Operation(summary = "Danh sách đơn đã nộp", description = """
            Lấy danh sách tất cả đơn ứng tuyển của ứng viên hiện tại.
            Có thể lọc theo trạng thái.
            Kết quả phân trang, sắp xếp theo ngày nộp mới nhất.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách đơn ứng tuyển của candidate (có phân trang)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    ResponseEntity<?> getApplications(
            @Parameter(description = "Lọc theo trạng thái: PENDING, REVIEWING, INTERVIEW, HIRED, REJECTED", example = "PENDING") String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") int page);

    @Operation(summary = "Chi tiết đơn ứng tuyển", description = "Xem chi tiết đơn ứng tuyển của mình: thông tin job đã ứng tuyển, cover letter, trạng thái hiện tại, ngày nộp.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chi tiết đơn ứng tuyển", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải đơn của bạn", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    ResponseEntity<?> getApplicationDetail(
            @Parameter(description = "ID của đơn ứng tuyển", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true) UUID id);

    @Operation(summary = "Lịch sử trạng thái đơn", description = """
            Xem timeline thay đổi trạng thái của đơn ứng tuyển.
            Mỗi lần employer cập nhật trạng thái (Pending → Reviewing → Interview → Hired/Rejected),
            một bản ghi mới được tạo kèm thời gian, trạng thái cũ, trạng thái mới.
            Dùng cho component ApplicationTimeline.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách các cập nhật trạng thái theo thời gian thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải đơn của bạn", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    ResponseEntity<?> getApplicationTimeline(
            @Parameter(description = "ID của đơn ứng tuyển", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true) UUID id);

    @Operation(summary = "Rút đơn ứng tuyển", description = """
            Rút/hủy đơn ứng tuyển của mình.
            Chỉ được rút khi đơn đang ở trạng thái PENDING.
            Sau khi rút, không thể khôi phục — cần nộp đơn mới nếu muốn ứng tuyển lại.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rút đơn thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Không thể rút đơn ở trạng thái hiện tại (chỉ PENDING mới được rút)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không phải đơn của bạn", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn ứng tuyển", content = @Content)
    })
    ResponseEntity<?> withdrawApplication(
            @Parameter(description = "ID của đơn ứng tuyển cần rút", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true) UUID id);

    @Operation(summary = "Xem CV gắn với đơn", description = """
            Xem hoặc tải CV (PDF) mà ứng viên đã đính kèm khi nộp đơn.
            Cả ứng viên (chủ đơn) và employer (chủ tin) đều có thể xem.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File CV PDF", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem CV của đơn này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hoặc CV", content = @Content)
    })
    ResponseEntity<?> getApplicationCV(
            @Parameter(description = "ID của đơn ứng tuyển", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", required = true) UUID id);
}
