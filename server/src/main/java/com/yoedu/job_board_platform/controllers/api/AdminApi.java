package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.UserRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin", description = "Quan tri he thong va kiem duyet. Yeu cau role ADMIN.")
public interface AdminApi {

    @Operation(summary = "Dashboard tổng quan")
    @ApiResponse(responseCode = "200", description = "Dữ liệu dashboard", content = @Content)
    ResponseEntity<?> getDashboard();

    @Operation(summary = "Danh sách người dùng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách người dùng", content = @Content),
            @ApiResponse(responseCode = "403", description = "Chỉ ADMIN mới được truy cập", content = @Content)
    })
    ResponseEntity<?> getUsers(
            @Parameter(description = "Lọc theo vai trò", example = "EMPLOYER") UserRole role,
            @Parameter(description = "Số trang bắt đầu từ 0", example = "0") int page);

    @Operation(summary = "Thống kê người dùng")
    @ApiResponse(responseCode = "200", description = "Dữ liệu thống kê người dùng", content = @Content)
    ResponseEntity<?> getUserStats();

    @Operation(summary = "Khóa tài khoản")
    ResponseEntity<?> suspendUser(
            @Parameter(description = "ID người dùng", required = true) UUID id);

    @Operation(summary = "Mở khóa tài khoản")
    ResponseEntity<?> reactivateUser(
            @Parameter(description = "ID người dùng", required = true) UUID id);

    @Operation(
            summary = "Danh sach cong ty cho duyet",
            description = "Tra ve danh sach cong ty status=PENDING cho ADMIN, co phan trang, tim kiem va loc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sach cong ty cho duyet", content = @Content),
            @ApiResponse(responseCode = "403", description = "Chi ADMIN moi duoc truy cap", content = @Content)
    })
    ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
            @Parameter(description = "So trang bat dau tu 0", example = "0") int page,
            @Parameter(description = "So cong ty moi trang", example = "10") int size,
            @Parameter(description = "Tu khoa tim theo ten, email, phone, taxCode, address, website") String keyword,
            @Parameter(description = "true: co ma so thue, false: thieu ma so thue") Boolean hasTaxCode,
            @Parameter(description = "true: co email/phone, false: thieu lien he") Boolean hasContact,
            @Parameter(description = "createdAt, companyName hoac taxCode", example = "createdAt") String sortBy,
            @Parameter(description = "asc hoac desc", example = "desc") String direction);

    @Operation(summary = "Duyet cong ty")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Duyet cong ty thanh cong", content = @Content),
            @ApiResponse(responseCode = "404", description = "Khong tim thay cong ty", content = @Content)
    })
    ResponseEntity<?> approveCompany(
            @Parameter(description = "ID cong ty can duyet", required = true) UUID id);

    @Operation(summary = "Tu choi cong ty")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tu choi cong ty thanh cong", content = @Content),
            @ApiResponse(responseCode = "400", description = "Thieu ly do tu choi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Khong tim thay cong ty", content = @Content)
    })
    ResponseEntity<?> rejectCompany(
            @Parameter(description = "ID cong ty can tu choi", required = true) UUID id,
            @Parameter(description = "Ly do tu choi", required = true) String reason);

    @Operation(summary = "Danh sách tất cả tin tuyển dụng")
    ResponseEntity<?> getAllJobs(
            @Parameter(description = "Lọc theo trạng thái", example = "PENDING_APPROVAL") String status,
            @Parameter(description = "Số trang bắt đầu từ 0", example = "0") int page);

    @Operation(summary = "Duyệt tin tuyển dụng")
    ResponseEntity<?> approveJob(
            @Parameter(description = "ID tin tuyển dụng", required = true) Long id);

    @Operation(summary = "Từ chối tin tuyển dụng")
    ResponseEntity<?> rejectJob(
            @Parameter(description = "ID tin tuyển dụng", required = true) Long id,
            @Parameter(description = "Lý do từ chối", required = true) String reason);

    @Operation(summary = "Xóa tin vi phạm")
    ResponseEntity<?> deleteJob(
            @Parameter(description = "ID tin tuyển dụng", required = true) Long id,
            @Parameter(description = "Lý do xóa") String reason);

    @Operation(summary = "Danh sách ngành nghề")
    ResponseEntity<?> getCategories();

    @Operation(summary = "Thêm ngành nghề mới")
    ResponseEntity<?> createCategory();

    @Operation(summary = "Cập nhật ngành nghề")
    ResponseEntity<?> updateCategory(
            @Parameter(description = "ID ngành nghề", required = true) Long id);

    @Operation(summary = "Xóa ngành nghề")
    ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID ngành nghề", required = true) Long id);
}
