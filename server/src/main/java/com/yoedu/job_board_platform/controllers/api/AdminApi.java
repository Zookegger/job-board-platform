package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminCompanyListResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminJobListResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminSkillResponse;
import com.yoedu.job_board_platform.dtos.admin.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.admin.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingJobResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillRequest;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.dtos.admin.AdminDashboardStatsResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminApplicationChartResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminUserListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Admin — Kiểm duyệt & Quản trị", description = "Quản trị hệ thống: thống kê, quản lý user/công ty/tin tuyển dụng, kiểm duyệt. Yêu cầu role ADMIN.")
public interface AdminApi {

        @Operation(summary = "Dashboard thống kê tổng quan", description = """
                        Lấy các chỉ số tổng quan cho màn hình Admin Dashboard:
                        tổng người dùng, tổng công ty, tổng tin tuyển dụng đã duyệt,
                        tổng hồ sơ ứng tuyển, số người dùng mới, số tin chờ duyệt
                        và số công ty chờ duyệt.
                        """)
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Dữ liệu thống kê tổng quan dashboard",
                        content = @Content)
        ResponseEntity<AdminDashboardStatsResponse> getDashboardStats();

        @Operation(summary = "Thống kê biểu đồ ứng tuyển", description = """
                Lấy dữ liệu thống kê đơn ứng tuyển cho trang Statistics:
                xu hướng số đơn ứng tuyển theo ngày và phân phối trạng thái ứng tuyển.
                Hỗ trợ query days = 7 hoặc 30.
                """)
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Dữ liệu thống kê biểu đồ ứng tuyển",
                        content = @Content)
        ResponseEntity<AdminApplicationChartResponse> getApplicationChartStats(
                        @Parameter(description = "Số ngày thống kê, chỉ hỗ trợ 7 hoặc 30")
                        int days
                );

        @Operation(summary = "Danh sách tài khoản", description = """
                Lấy danh sách tất cả tài khoản trong hệ thống cho Admin.
                Hỗ trợ lọc theo role và trạng thái tài khoản, có phân trang.
                role: ADMIN, EMPLOYER, CANDIDATE hoặc ALL.
                status: ACTIVE, INACTIVE hoặc ALL.
                """)
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Danh sách tài khoản",
                        content = @Content)
        ResponseEntity<Page<AdminUserListResponse>> getUsers(
                        @Parameter(description = "Lọc theo vai trò: ADMIN, EMPLOYER, CANDIDATE hoặc ALL", example = "CANDIDATE")
                        String role,

                        @Parameter(description = "Lọc theo trạng thái: ACTIVE, INACTIVE hoặc ALL", example = "ACTIVE")
                        String status,

                        @ParameterObject @PageableDefault(page = 0, size = 10)
                        Pageable pageable
                );

        @Operation(summary = "Khóa tài khoản", description = """
                        Khóa tài khoản người dùng. User bị khóa sẽ không thể đăng nhập hoặc sử dụng hệ thống.
                        Có thể khóa bất kỳ tài khoản nào (CANDIDATE, EMPLOYER, ADMIN).
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Khóa tài khoản thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể khóa tài khoản ADMIN khác", content = @Content)
        })
        ResponseEntity<?> suspendUser(
                        @Parameter(description = "ID của người dùng cần khóa", example = "1", required = true) UUID id);

        @Operation(summary = "Mở khóa tài khoản", description = "Mở khóa tài khoản đã bị khóa trước đó. User có thể đăng nhập và sử dụng hệ thống trở lại.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mở khóa tài khoản thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content)
        })
        ResponseEntity<?> reactivateUser(
                        @Parameter(description = "ID của người dùng cần mở khóa", example = "1", required = true) UUID id);

        @Operation(summary = "Danh sách công ty chờ duyệt", description = """
                        Lấy danh sách các công ty đang chờ admin phê duyệt (status = PENDING).
                        Hỗ trợ tìm kiếm, lọc và phân trang. Dùng cho màn hình kiểm duyệt công ty.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách công ty chờ duyệt (có phân trang)", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ ADMIN)", content = @Content)
        })
        ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
                        @Parameter(description = "Từ khóa tìm theo tên, email, phone, mã số thuế, địa chỉ, website") String keyword,
                        @Parameter(description = "true: có mã số thuế, false: thiếu mã số thuế") Boolean hasTaxCode,
                        @Parameter(description = "true: có email/phone, false: thiếu liên hệ") Boolean hasContact,
                        @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable);

        @Operation(summary = "Danh sách tất cả công ty", description = "Lấy danh sách tất cả công ty, hỗ trợ tìm kiếm, lọc theo trạng thái và phân trang.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách công ty (có phân trang)", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ ADMIN)", content = @Content)
        })
        ResponseEntity<Page<AdminCompanyListResponse>> getAllCompanies(
                        @Parameter(description = "Từ khóa tìm theo tên, email, phone, mã số thuế, địa chỉ, website") String keyword,
                        @Parameter(description = "Lọc theo trạng thái: PENDING, APPROVED, REJECTED, SUSPENDED") String status,
                        @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable);

        @Operation(summary = "Duyệt công ty", description = "Phê duyệt công ty — sau khi duyệt, employer có thể đăng tin tuyển dụng. Hệ thống gửi email thông báo cho employer.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Duyệt công ty thành công — employer nhận được email thông báo", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy công ty", content = @Content)
        })
        ResponseEntity<?> approveCompany(
                        @Parameter(description = "ID công ty cần duyệt", required = true) UUID id);

        @Operation(summary = "Từ chối công ty", description = "Từ chối phê duyệt công ty kèm lý do. Hệ thống gửi thông báo kèm lý do từ chối cho employer.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Từ chối công ty thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thiếu lý do từ chối", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy công ty", content = @Content)
        })
        ResponseEntity<?> rejectCompany(
                        @Parameter(description = "ID công ty cần từ chối", required = true) UUID id,
                        @Valid @RequestBody(description = "Lý do từ chối", required = true) CompanyRejectionRequest request);

        @Operation(summary = "Tạm ngưng công ty", description = "Tạm ngưng công ty kèm lý do. Hệ thống gửi thông báo cho employer.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạm ngưng công ty thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thiếu lý do tạm ngưng", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy công ty", content = @Content)
        })
        ResponseEntity<?> suspendCompany(
                        @Parameter(description = "ID công ty cần tạm ngưng", required = true) UUID id,
                        @Valid @RequestBody(description = "Lý do tạm ngưng", required = true) CompanySuspensionRequest request);

        @Operation(summary = "Mở tạm ngưng công ty", description = "Khôi phục công ty từ trạng thái SUSPENDED về APPROVED. Hệ thống gửi thông báo cho employer.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mở tạm ngưng công ty thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Công ty không ở trạng thái tạm ngưng"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy công ty")
        })
        ResponseEntity<ApiResponse> unsuspendCompany(
                        @Parameter(description = "ID công ty cần mở tạm ngưng", required = true) UUID id);

        // ================ Jobs ================

        @Operation(summary = "Danh sách tất cả tin tuyển dụng", description = """
                        Lấy danh sách tất cả tin tuyển dụng trên hệ thống, bao gồm cả tin của tất cả công ty.
                        Có thể lọc theo trạng thái. Dùng cho màn hình quản lý tin toàn hệ thống.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách tất cả tin tuyển dụng", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
        })
        ResponseEntity<Page<AdminJobListResponse>> getAllJobs(
                        @Parameter(description = "Lọc theo trạng thái: DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, REJECTED", example = "PENDING_APPROVAL") String status,
                        @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable);

        @Operation(summary = "Danh sách tin tuyển dụng chờ duyệt", description = "Lấy danh sách tin tuyển dụng với trạng thái PENDING_APPROVAL.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách tin chờ duyệt (có phân trang)", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
        })
        ResponseEntity<Page<PendingJobResponse>> getPendingJobs(
                        @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable);

        @Operation(summary = "Duyệt tin tuyển dụng", description = "Phê duyệt tin tuyển dụng — chuyển trạng thái từ PENDING_APPROVAL sang ACTIVE.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Duyệt tin thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tin không ở trạng thái PENDING_APPROVAL", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<ApiResponse> approveJob(
                        @Parameter(description = "ID của tin tuyển dụng cần duyệt", required = true) UUID id);

        @Operation(summary = "Từ chối tin tuyển dụng", description = "Từ chối tin tuyển dụng kèm lý do. Hệ thống gửi email thông báo cho employer.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Từ chối tin thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thiếu lý do từ chối", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<ApiResponse> rejectJob(
                        @Parameter(description = "ID của tin tuyển dụng cần từ chối", required = true) UUID id,
                        @Valid @RequestBody @Parameter(description = "Lý do từ chối", required = true) com.yoedu.job_board_platform.dtos.admin.JobRejectRequest request);

        @Operation(summary = "Xóa tin vi phạm", description = """
                        Xóa tin tuyển dụng vi phạm chính sách nền tảng.
                        Có thể kèm lý do xóa để ghi log.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa tin thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
        })
        ResponseEntity<ApiResponse> deleteJob(
                        @Parameter(description = "ID của tin tuyển dụng cần xóa", required = true) UUID id,
                        @Parameter(description = "Lý do xóa", example = "Nội dung vi phạm chính sách") String reason);

        // ================ Reports ================

        @Operation(summary = "Danh sách báo cáo vi phạm", description = """
                        Lấy danh sách tất cả báo cáo vi phạm trên hệ thống.
                        Có thể lọc theo trạng thái. Dùng cho màn hình quản lý báo cáo của admin.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách báo cáo (có phân trang)", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ ADMIN)", content = @Content)
        })
        ResponseEntity<Page<com.yoedu.job_board_platform.dtos.report.ReportResponse>> getReports(
                        @Parameter(description = "Lọc theo trạng thái: PENDING, REVIEWED, DISMISSED, RESOLVED", example = "PENDING") com.yoedu.job_board_platform.models.ReportStatus status,
                        @ParameterObject Pageable pageable);

        @Operation(summary = "Xem xét báo cáo", description = "Xem xét báo cáo — chuyển trạng thái từ PENDING sang REVIEWED.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xem xét báo cáo thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Báo cáo không ở trạng thái PENDING", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy báo cáo", content = @Content)
        })
        ResponseEntity<ApiResponse> reviewReport(
                        @Parameter(description = "ID báo cáo cần xem xét", required = true) UUID id,
                        @RequestBody(description = "Ghi chú xử lý (tuỳ chọn)", required = false) com.yoedu.job_board_platform.dtos.report.AdminReportActionRequest request);

        @Operation(summary = "Bác bỏ báo cáo", description = "Bác bỏ báo cáo — chuyển trạng thái từ PENDING/REVIEWED sang DISMISSED.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bác bỏ báo cáo thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Báo cáo đã được xử lý, không thể bác bỏ", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy báo cáo", content = @Content)
        })
        ResponseEntity<ApiResponse> dismissReport(
                        @Parameter(description = "ID báo cáo cần bác bỏ", required = true) UUID id,
                        @RequestBody(description = "Ghi chú xử lý (tuỳ chọn)", required = false) com.yoedu.job_board_platform.dtos.report.AdminReportActionRequest request);

        @Operation(summary = "Giải quyết báo cáo", description = "Giải quyết báo cáo — chuyển trạng thái từ REVIEWED sang RESOLVED.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Giải quyết báo cáo thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Báo cáo phải ở trạng thái REVIEWED trước khi giải quyết", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy báo cáo", content = @Content)
        })
        ResponseEntity<ApiResponse> resolveReport(
                        @Parameter(description = "ID báo cáo cần giải quyết", required = true) UUID id,
                        @RequestBody(description = "Ghi chú xử lý (tuỳ chọn)", required = false) com.yoedu.job_board_platform.dtos.report.AdminReportActionRequest request);

        // ================ Skills ================

        @Operation(summary = "Danh sách kỹ năng (Admin)", description = "Lấy danh sách tất cả kỹ năng hệ thống (bao gồm cả kỹ năng đã bị ẩn/tắt). Hỗ trợ lọc theo từ khóa tên và trạng thái hoạt động. Phục vụ cho giao diện quản trị.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách phân trang kỹ năng hệ thống"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ ADMIN)", content = @Content)
        })
        ResponseEntity<Page<AdminSkillResponse>> getAllSkills(
                        @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable,
                        @Parameter(description = "Bộ lọc: từ khóa tên kỹ năng và trạng thái hoạt động (null: tất cả, true: active, false: inactive)") SkillFilterRequest request);

        @Operation(summary = "Thêm kỹ năng mới", description = "Tạo một kỹ năng mới. Tên kỹ năng phải chưa tồn tại, nếu không sẽ trả về lỗi 409 Conflict.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo kỹ năng thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (tên trống hoặc quá dài)", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tên kỹ năng đã tồn tại (Conflict)", content = @Content)
        })
        ResponseEntity<AdminSkillResponse> createSkill(
                        @RequestBody @Parameter(description = "Thông tin kỹ năng mới (name bắt buộc, isActive tùy chọn)", required = true) SkillRequest request);

        @Operation(summary = "Cập nhật kỹ năng", description = "Chỉnh sửa thông tin kỹ năng. Kiểm tra trùng tên nếu tên được thay đổi.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật kỹ năng thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy kỹ năng", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tên kỹ năng đã tồn tại (Conflict)", content = @Content)
        })
        ResponseEntity<AdminSkillResponse> updateSkill(
                        @Parameter(description = "ID kỹ năng cần sửa", example = "1", required = true) Integer id,
                        @RequestBody @Parameter(description = "Thông tin kỹ năng mới", required = true) SkillRequest request);

        @Operation(summary = "Bật/tắt kỹ năng", description = "Đảo ngược trạng thái hoạt động (isActive) của kỹ năng. Kỹ năng bị tắt sẽ không hiển thị cho ứng viên khi chọn kỹ năng.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy kỹ năng", content = @Content)
        })
        ResponseEntity<AdminSkillResponse> toggleSkillStatus(
                        @Parameter(description = "ID kỹ năng cần bật/tắt", example = "1", required = true) Integer id);

        @Operation(summary = "Xóa kỹ năng", description = "Xóa kỹ năng khỏi hệ thống. Các bản ghi liên quan trong job_skills và candidate_skills cũng bị xóa theo.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa kỹ năng thành công", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy kỹ năng", content = @Content)
        })
        ResponseEntity<?> deleteSkill(
                        @Parameter(description = "ID kỹ năng cần xóa", example = "1", required = true) Integer id);
}