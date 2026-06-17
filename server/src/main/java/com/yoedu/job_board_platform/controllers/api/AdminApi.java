package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.company.CompanyApprovalRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.company.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.company.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.UserRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin — Kiểm duyệt & Quản trị", description = "Quản trị hệ thống: thống kê, quản lý user/công ty/tin tuyển dụng/ngành nghề, kiểm duyệt. Yêu cầu role ADMIN.")
public interface AdminApi {

    @Operation(summary = "Dashboard tổng quan", description = """
            Thống kê toàn nền tảng: tổng số user, tổng số công ty, tổng số tin tuyển dụng,
            số lượng user mới, số tin chờ duyệt, số công ty chờ duyệt, ...
            Dùng cho màn hình Admin Dashboard.
            """)
    @ApiResponse(responseCode = "200", description = "Dữ liệu thống kê tổng quan nền tảng", content = @Content)
    ResponseEntity<?> getDashboard();

    @Operation(summary = "Danh sách người dùng", description = """
            Lấy danh sách tất cả người dùng trên hệ thống (CANDIDATE, EMPLOYER, ADMIN).
            Có thể lọc theo role và phân trang.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách người dùng (có phân trang)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ ADMIN)", content = @Content)
    })
    ResponseEntity<?> getUsers(
            @Parameter(description = "Lọc theo vai trò: CANDIDATE, EMPLOYER, ADMIN", example = "CANDIDATE") UserRole role,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") int page);

    @Operation(summary = "Thống kê người dùng", description = "Thống kê số lượng user theo role, số user đăng ký mới theo ngày/tuần/tháng.")
    @ApiResponse(responseCode = "200", description = "Dữ liệu thống kê người dùng", content = @Content)
    ResponseEntity<?> getUserStats();

    @Operation(summary = "Khóa tài khoản", description = """
            Khóa tài khoản người dùng. User bị khóa sẽ không thể đăng nhập hoặc sử dụng hệ thống.
            Có thể khóa bất kỳ tài khoản nào (CANDIDATE, EMPLOYER, ADMIN).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Khóa tài khoản thành công", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content),
            @ApiResponse(responseCode = "400", description = "Không thể khóa tài khoản ADMIN khác", content = @Content)
    })
    ResponseEntity<?> suspendUser(
            @Parameter(description = "ID của người dùng cần khóa", example = "1", required = true) UUID id);

    @Operation(summary = "Mở khóa tài khoản", description = "Mở khóa tài khoản đã bị khóa trước đó. User có thể đăng nhập và sử dụng hệ thống trở lại.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mở khóa tài khoản thành công", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content)
    })
    ResponseEntity<?> reactivateUser(
            @Parameter(description = "ID của người dùng cần mở khóa", example = "1", required = true) UUID id);

    @Operation(summary = "Danh sách công ty chờ duyệt", description = """
            Lấy danh sách các công ty đang chờ admin phê duyệt (status = PENDING).
            Dùng cho màn hình kiểm duyệt công ty.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách công ty chờ duyệt (có phân trang)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
   ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") int page,
        @Parameter(description = "Số lượng phần tử mỗi trang", example = "20") int size);

    @Operation(summary = "Duyệt công ty", description = "Phê duyệt công ty — sau khi duyệt, employer có thể đăng tin tuyển dụng. Hệ thống gửi email thông báo cho employer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyệt công ty thành công — employer nhận được email thông báo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty", content = @Content)
    })
    ResponseEntity<?> approveCompany(
        @Parameter(description = "ID của công ty cần duyệt", required = true) UUID id,
        CompanyApprovalRequest request);

    @Operation(summary = "Từ chối công ty", description = "Từ chối phê duyệt công ty kèm lý do. Hệ thống gửi email thông báo kèm lý do từ chối cho employer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Từ chối công ty thành công — email thông báo đã được gửi", content = @Content),
            @ApiResponse(responseCode = "400", description = "Thiếu lý do từ chối", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty", content = @Content)
    })
    ResponseEntity<?> rejectCompany(
        @Parameter(description = "ID của công ty cần từ chối", required = true) UUID id,
        CompanyRejectionRequest request);

        @Operation(summary = "Tạm ngưng công ty", description = "Tạm ngưng công ty kèm lý do.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Tạm ngưng công ty thành công", content = @Content),
                @ApiResponse(responseCode = "400", description = "Thiếu lý do tạm ngưng", content = @Content),
                @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty", content = @Content)
})
        ResponseEntity<?> suspendCompany(
                @Parameter(description = "ID của công ty cần tạm ngưng", required = true) UUID id,
                CompanySuspensionRequest request);

    @Operation(summary = "Danh sách tất cả tin tuyển dụng", description = """
            Lấy danh sách tất cả tin tuyển dụng trên hệ thống, bao gồm cả tin của tất cả công ty.
            Có thể lọc theo trạng thái. Dùng cho màn hình quản lý tin toàn hệ thống.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách tất cả tin tuyển dụng (có phân trang)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    ResponseEntity<?> getAllJobs(
            @Parameter(description = "Lọc theo trạng thái: DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, REJECTED", example = "PENDING_APPROVAL") String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") int page);

    @Operation(summary = "Duyệt tin tuyển dụng", description = "Phê duyệt tin tuyển dụng — chuyển trạng thái từ PENDING_APPROVAL sang ACTIVE. Tin sẽ hiển thị công khai cho ứng viên.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyệt tin thành công — tin chuyển sang ACTIVE", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    ResponseEntity<?> approveJob(
            @Parameter(description = "ID của tin tuyển dụng cần duyệt", example = "1", required = true) Long id);

    @Operation(summary = "Từ chối tin tuyển dụng", description = "Từ chối tin tuyển dụng kèm lý do. Hệ thống gửi email thông báo cho employer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Từ chối tin thành công — email thông báo đã được gửi", content = @Content),
            @ApiResponse(responseCode = "400", description = "Thiếu lý do từ chối", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    ResponseEntity<?> rejectJob(
            @Parameter(description = "ID của tin tuyển dụng cần từ chối", example = "1", required = true) Long id,
            @Parameter(description = "Lý do từ chối (bắt buộc)", example = "Nội dung không phù hợp", required = true) String reason);

    @Operation(summary = "Xóa tin vi phạm", description = """
            Xóa tin tuyển dụng vi phạm chính sách nền tảng.
            Có thể kèm lý do xóa để ghi log.
            Tin sẽ bị xóa vĩnh viễn khỏi hệ thống.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa tin thành công", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    ResponseEntity<?> deleteJob(
            @Parameter(description = "ID của tin tuyển dụng cần xóa", example = "1", required = true) Long id,
            @Parameter(description = "Lý do xóa (ghi log)", example = "Nội dung vi phạm chính sách") String reason);

    @Operation(summary = "Danh sách ngành nghề", description = "Lấy danh sách tất cả ngành nghề đang có trong hệ thống.")
    @ApiResponse(responseCode = "200", description = "Danh sách ngành nghề", content = @Content)
    ResponseEntity<?> getCategories();

    @Operation(summary = "Thêm ngành nghề mới", description = "Tạo một ngành nghề mới (ví dụ: Công nghệ thông tin, Kế toán, Xây dựng...). Tên ngành phải chưa tồn tại.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo ngành nghề mới thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Tên ngành đã tồn tại hoặc dữ liệu không hợp lệ", content = @Content)
    })
    ResponseEntity<?> createCategory();

    @Operation(summary = "Cập nhật ngành nghề", description = "Chỉnh sửa thông tin ngành nghề (tên, mô tả).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật ngành nghề thành công", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ngành nghề", content = @Content)
    })
    ResponseEntity<?> updateCategory(
            @Parameter(description = "ID của ngành nghề cần sửa", example = "1", required = true) Long id);

    @Operation(summary = "Xóa ngành nghề", description = """
            Xóa ngành nghề khỏi hệ thống.
            Chỉ xóa được nếu không có công việc nào đang thuộc ngành này.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa ngành nghề thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Không thể xóa — ngành đang có công việc liên kết", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ngành nghề", content = @Content)
    })
    ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID của ngành nghề cần xóa", example = "1", required = true) Long id);
}
