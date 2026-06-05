package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Kiểm duyệt & Quản trị", description = "Quản trị hệ thống - chỉ ADMIN")
public class AdminController {

    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard thống kê nền tảng")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok("Dashboard thống kê");
    }

    // ========== QUẢN LÝ USER ==========
    @GetMapping("/users")
    @Operation(summary = "Danh sách tất cả user", description = "Phân trang, lọc theo role")
    public ResponseEntity<?> getUsers(
            @Parameter(description = "Role (CANDIDATE, EMPLOYER, ADMIN)")
            @RequestParam(required = false) String role,
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách user");
    }

    @GetMapping("/users/stats")
    @Operation(summary = "Thống kê user", description = "User theo role / ngày")
    public ResponseEntity<?> getUserStats() {
        return ResponseEntity.ok("Thống kê user");
    }

    @PostMapping("/users/{id}/suspend")
    @Operation(summary = "Khóa tài khoản")
    public ResponseEntity<?> suspendUser(
            @Parameter(description = "User ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Khóa tài khoản thành công");
    }

    @PostMapping("/users/{id}/reactivate")
    @Operation(summary = "Mở khóa tài khoản")
    public ResponseEntity<?> reactivateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Mở khóa thành công");
    }
    @GetMapping("/companies/pending")
    @Operation(summary = "Danh sách công ty chờ duyệt")
    public ResponseEntity<?> getPendingCompanies(
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách công ty chờ duyệt");
    }

    @PostMapping("/companies/{id}/approve")
    @Operation(summary = "Duyệt công ty")
    public ResponseEntity<?> approveCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Duyệt công ty thành công");
    }

    @PostMapping("/companies/{id}/reject")
    @Operation(summary = "Từ chối công ty", description = "Gửi email từ chối kèm lý do")
    public ResponseEntity<?> rejectCompany(
            @Parameter(description = "Company ID")
            @PathVariable Long id,
            @Parameter(description = "Lý do từ chối")
            @RequestParam String reason
    ) {
        return ResponseEntity.ok("Từ chối công ty");
    }

    // ========== QUẢN LÝ TIN TUYỂN DỤNG ==========
    @GetMapping("/jobs")
    @Operation(summary = "Danh sách tất cả tin", description = "Lọc theo status")
    public ResponseEntity<?> getAllJobs(
            @Parameter(description = "Trạng thái (DRAFT, PUBLISHED, REJECTED, PENDING_APPROVAL)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping("/jobs/{id}/approve")
    @Operation(summary = "Duyệt tin tuyển dụng")
    public ResponseEntity<?> approveJob(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Duyệt tin thành công");
    }

    @PostMapping("/jobs/{id}/reject")
    @Operation(summary = "Từ chối tin", description = "Gửi email từ chối kèm lý do")
    public ResponseEntity<?> rejectJob(
            @Parameter(description = "Job ID")
            @PathVariable Long id,
            @Parameter(description = "Lý do từ chối")
            @RequestParam String reason
    ) {
        return ResponseEntity.ok("Từ chối tin");
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Xóa tin vi phạm")
    public ResponseEntity<?> deleteJob(
            @Parameter(description = "Job ID")
            @PathVariable Long id,
            @Parameter(description = "Lý do xóa")
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok("Xóa tin thành công");
    }

    //QUẢN LÝ NGÀNH NGHỀ 
    @GetMapping("/categories")
    @Operation(summary = "Danh sách ngành nghề")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sách ngành");
    }

    @PostMapping("/categories")
    @Operation(summary = "Tạo ngành mới")
    public ResponseEntity<?> createCategory() {
        return ResponseEntity.ok("Tạo ngành thành công");
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Sửa ngành")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Cập nhật ngành thành công");
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Xóa ngành")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Xóa ngành thành công");
    }
}
