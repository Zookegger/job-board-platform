package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@PreAuthorize("hasRole('EMPLOYER')")
@Tag(name = "🏢 Nhà tuyển dụng — Quản lý tin", description = "Quản lý công việc tuyển dụng - chỉ EMPLOYER")
public class EmployerJobController {

    @GetMapping
    @Operation(summary = "Danh sách tin của công ty")
    public ResponseEntity<?> getMyJobs(
            @Parameter(description = "Trạng thái (DRAFT, PUBLISHED, CLOSED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping
    @Operation(summary = "Đăng tin tuyển dụng mới")
    public ResponseEntity<?> createJob() {
        return ResponseEntity.ok("Tạo tin thành công");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết tin (có thể edit)")
    public ResponseEntity<?> getJobDetail(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết tin");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tin tuyển dụng")
    public ResponseEntity<?> updateJob(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tin tuyển dụng")
    public ResponseEntity<?> deleteJob(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Xóa thành công");
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "Đóng / mở tuyển dụng")
    public ResponseEntity<?> toggleJobStatus(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Thay đổi trạng thái thành công");
    }

    @GetMapping("/my-company")
    @Operation(summary = "Xem thông tin công ty của mình")
    public ResponseEntity<?> getMyCompany() {
        return ResponseEntity.ok("Thông tin công ty");
    }

    @PutMapping("/my-company")
    @Operation(summary = "Cập nhật thông tin công ty")
    public ResponseEntity<?> updateMyCompany() {
        return ResponseEntity.ok("Cập nhật công ty thành công");
    }

    @GetMapping("/employer-dashboard")
    @Operation(summary = "Dashboard NTD", description = "Thống kê tin, ứng viên, ...")
    public ResponseEntity<?> getEmployerDashboard() {
        return ResponseEntity.ok("Dashboard thống kê");
    }
}
