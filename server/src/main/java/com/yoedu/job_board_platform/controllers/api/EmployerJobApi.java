package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.job.JobRequest;
import com.yoedu.job_board_platform.models.JobStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Nhà tuyển dụng — Quản lý tin", description = "CRUD tin tuyển dụng, quản lý trạng thái, thông tin công ty, dashboard. Yêu cầu role EMPLOYER.")
public interface EmployerJobApi {

        @Operation(summary = "Danh sách tin của công ty", description = """
                        Lấy danh sách tin tuyển dụng thuộc về công ty của employer đang đăng nhập.
                        Có thể lọc theo trạng thái (DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, REJECTED).
                        Kết quả phân trang, sắp xếp theo ngày tạo mới nhất.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Danh sách tin tuyển dụng của công ty (có phân trang)", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content)
        })
        ResponseEntity<?> getEmployerJobs(
                        @Parameter(description = "Lọc theo trạng thái: DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, REJECTED", example = "ACTIVE") JobStatus status,
                        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") int page,
                        @Parameter(description = "Số lượng mỗi trang", example = "20") int size);

        @Operation(summary = "Đăng tin tuyển dụng mới", description = """
                        Tạo một tin tuyển dụng mới cho công ty của employer.
                        Tin sẽ ở trạng thái DRAFT ban đầu, sau đó cần gửi duyệt để chờ admin phê duyệt.
                        Yêu cầu body chứa đầy đủ thông tin: tiêu đề, mô tả, yêu cầu, mức lương, địa điểm, loại hình...
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Tạo tin tuyển dụng thành công (trạng thái DRAFT)", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (thiếu trường bắt buộc, sai định dạng)", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
        })
        ResponseEntity<?> createJob(
                        @Parameter(description = "Thông tin tin tuyển dụng", required = true) JobRequest request);

        @Operation(summary = "Chi tiết tin (có thể chỉnh sửa)", description = """
                        Lấy chi tiết tin tuyển dụng của công ty mình để xem và chỉnh sửa.
                        Trả về toàn bộ thông tin bao gồm cả các trường admin không thấy được.
                        Chỉ trả về nếu tin thuộc về công ty của employer hiện tại.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Chi tiết tin tuyển dụng (đầy đủ)", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Tin không thuộc về công ty của bạn", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<?> getJobDetail(
                        @Parameter(description = "ID của tin tuyển dụng", example = "550e8400-e29b-41d4-a716-446655440000", required = true) UUID id);

        @Operation(summary = "Cập nhật tin tuyển dụng", description = """
                        Cập nhật thông tin tin tuyển dụng đã đăng.
                        Chỉ cập nhật được các tin thuộc về công ty của mình.
                        Nếu tin đang ở trạng thái không phải DRAFT, tin sẽ được đưa về trạng thái DRAFT sau khi cập nhật.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cập nhật tin tuyển dụng thành công", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu cập nhật không hợp lệ", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền sửa tin này", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<?> updateJob(
                        @Parameter(description = "ID của tin tuyển dụng cần cập nhật", example = "550e8400-e29b-41d4-a716-446655440000", required = true) UUID id,
                        @Parameter(description = "Thông tin cập nhật", required = true) JobRequest request);

        @Operation(summary = "Gửi duyệt tin tuyển dụng", description = """
                        Gửi tin tuyển dụng ở trạng thái DRAFT cho admin phê duyệt.
                        Chuyển trạng thái từ DRAFT sang PENDING_APPROVAL.
                        Chỉ thực hiện được khi tin đang ở trạng thái DRAFT.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Gửi duyệt thành công", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Tin không ở trạng thái DRAFT", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền thao tác", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<?> submitForReview(
                        @Parameter(description = "ID của tin tuyển dụng", example = "550e8400-e29b-41d4-a716-446655440000", required = true) UUID id);

        @Operation(summary = "Xóa tin tuyển dụng", description = """
                        Xóa vĩnh viễn một tin tuyển dụng của công ty mình.
                        Hành động này không thể hoàn tác — tất cả đơn ứng tuyển liên quan cũng sẽ bị xóa.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Xóa tin tuyển dụng thành công", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Không có quyền xóa tin này", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<?> deleteJob(
                        @Parameter(description = "ID của tin tuyển dụng cần xóa", example = "550e8400-e29b-41d4-a716-446655440000", required = true) UUID id);

}
