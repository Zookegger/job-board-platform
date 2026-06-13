package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

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

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.AdminApi;
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
        return ResponseEntity.ok("Dashboard thống kê");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách user");
    }

    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats() {
        return ResponseEntity.ok("Thống kê user");
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Khóa tài khoản thành công");
    }

    @PostMapping("/users/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Mở khóa thành công");
    }

    @GetMapping("/companies/pending")
    public ResponseEntity<?> getPendingCompanies(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sách công ty chờ duyệt");
    }

    @PostMapping("/companies/{id}/approve")
    public ResponseEntity<?> approveCompany(@PathVariable UUID id) {
        adminService.approveCompany(id);
        return ResponseEntity.ok("Duyệt công ty thành công");
    }

    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<?> rejectCompany(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok("Từ chối công ty");
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping("/jobs/{id}/approve")
    public ResponseEntity<?> approveJob(@PathVariable Long id) {
        return ResponseEntity.ok("Duyệt tin thành công");
    }

    @PostMapping("/jobs/{id}/reject")
    public ResponseEntity<?> rejectJob(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok("Từ chối tin");
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok("Xóa tin thành công");
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sách ngành");
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory() {
        return ResponseEntity.ok("Tạo ngành thành công");
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Cập nhật ngành thành công");
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Xóa ngành thành công");
    }
}
