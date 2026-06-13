package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
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

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.AdminApi;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.services.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController implements AdminApi {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok("Dashboard thong ke");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sach user");
    }

    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats() {
        return ResponseEntity.ok("Thong ke user");
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Khoa tai khoan thanh cong");
    }

    @PostMapping("/users/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Mo khoa thanh cong");
    }

    @GetMapping("/companies/pending")
    public ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasTaxCode,
            @RequestParam(required = false) Boolean hasContact,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(adminService.getPendingCompanies(
                page,
                size,
                keyword,
                hasTaxCode,
                hasContact,
                sortBy,
                direction));
    }

    @PostMapping("/companies/{id}/approve")
    public ResponseEntity<ApiResponse> approveCompany(@PathVariable UUID id) {
        adminService.approveCompany(id);
        return ResponseEntity.ok(new ApiResponse("Duyet cong ty thanh cong"));
    }

    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<ApiResponse> rejectCompany(@PathVariable UUID id, @RequestParam String reason) {
        adminService.rejectCompany(id, reason);
        return ResponseEntity.ok(new ApiResponse("Tu choi cong ty thanh cong"));
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sach tin");
    }

    @PostMapping("/jobs/{id}/approve")
    public ResponseEntity<?> approveJob(@PathVariable Long id) {
        return ResponseEntity.ok("Duyet tin thanh cong");
    }

    @PostMapping("/jobs/{id}/reject")
    public ResponseEntity<?> rejectJob(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok("Tu choi tin");
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok("Xoa tin thanh cong");
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sach nganh");
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory() {
        return ResponseEntity.ok("Tao nganh thanh cong");
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Cap nhat nganh thanh cong");
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Xoa nganh thanh cong");
    }
}
