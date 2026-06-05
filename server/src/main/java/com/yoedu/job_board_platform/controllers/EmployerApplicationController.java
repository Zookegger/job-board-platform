package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer/applications")
@PreAuthorize("hasRole('EMPLOYER')")
@Tag(name = "Nhà tuyển dụng — Quản lý ứng viên", description = "Quản lý hồ sơ ứng viên - chỉ EMPLOYER")
public class EmployerApplicationController {

    @GetMapping
    @Operation(summary = "CandidateTable — danh sách ứng viên", 
               description = "Lọc theo job_id, status, phân trang")
    public ResponseEntity<?> getApplications(
            @Parameter(description = "Job ID")
            @RequestParam(required = false) Long jobId,
            @Parameter(description = "Trạng thái (PENDING, REVIEWING, INTERVIEW, HIRED, REJECTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách ứng viên");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết hồ sơ ứng viên")
    public ResponseEntity<?> getApplicationDetail(
            @Parameter(description = "Application ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết hồ sơ");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái ứng viên",
               description = "Thay đổi sang: Reviewing / Interview / Hired / Rejected")
    public ResponseEntity<?> updateApplicationStatus(
            @Parameter(description = "Application ID")
            @PathVariable Long id,
            @Parameter(description = "Trạng thái mới")
            @RequestParam String status,
            @Parameter(description = "Lý do (nếu reject)")
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
