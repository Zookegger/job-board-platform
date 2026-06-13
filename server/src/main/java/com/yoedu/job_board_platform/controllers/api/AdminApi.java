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

    @Operation(summary = "Dashboard tong quan")
    @ApiResponse(responseCode = "200", description = "Du lieu dashboard", content = @Content)
    ResponseEntity<?> getDashboard();

    @Operation(summary = "Danh sach nguoi dung")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sach nguoi dung", content = @Content),
            @ApiResponse(responseCode = "403", description = "Chi ADMIN moi duoc truy cap", content = @Content)
    })
    ResponseEntity<?> getUsers(
            @Parameter(description = "Loc theo vai tro", example = "EMPLOYER") UserRole role,
            @Parameter(description = "So trang bat dau tu 0", example = "0") int page);

    @Operation(summary = "Thong ke nguoi dung")
    @ApiResponse(responseCode = "200", description = "Du lieu thong ke nguoi dung", content = @Content)
    ResponseEntity<?> getUserStats();

    @Operation(summary = "Khoa tai khoan")
    ResponseEntity<?> suspendUser(
            @Parameter(description = "ID nguoi dung", required = true) UUID id);

    @Operation(summary = "Mo khoa tai khoan")
    ResponseEntity<?> reactivateUser(
            @Parameter(description = "ID nguoi dung", required = true) UUID id);

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

    @Operation(summary = "Danh sach tat ca tin tuyen dung")
    ResponseEntity<?> getAllJobs(
            @Parameter(description = "Loc theo trang thai", example = "PENDING_APPROVAL") String status,
            @Parameter(description = "So trang bat dau tu 0", example = "0") int page);

    @Operation(summary = "Duyet tin tuyen dung")
    ResponseEntity<?> approveJob(
            @Parameter(description = "ID tin tuyen dung", required = true) Long id);

    @Operation(summary = "Tu choi tin tuyen dung")
    ResponseEntity<?> rejectJob(
            @Parameter(description = "ID tin tuyen dung", required = true) Long id,
            @Parameter(description = "Ly do tu choi", required = true) String reason);

    @Operation(summary = "Xoa tin vi pham")
    ResponseEntity<?> deleteJob(
            @Parameter(description = "ID tin tuyen dung", required = true) Long id,
            @Parameter(description = "Ly do xoa") String reason);

    @Operation(summary = "Danh sach nganh nghe")
    ResponseEntity<?> getCategories();

    @Operation(summary = "Them nganh nghe moi")
    ResponseEntity<?> createCategory();

    @Operation(summary = "Cap nhat nganh nghe")
    ResponseEntity<?> updateCategory(
            @Parameter(description = "ID nganh nghe", required = true) Long id);

    @Operation(summary = "Xoa nganh nghe")
    ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID nganh nghe", required = true) Long id);
}
