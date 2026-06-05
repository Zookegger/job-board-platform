package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Ứng tuyển & Timeline", description = "Quản lý đơn ứng tuyển - chỉ CANDIDATE")
public class CandidateApplicationController {

    @PostMapping
    @Operation(summary = "Nộp hồ sơ ứng tuyển")
    public ResponseEntity<?> submitApplication() {
        return ResponseEntity.ok("Nộp hồ sơ thành công");
    }

    @GetMapping
    @Operation(summary = "Danh sách đơn đã nộp")
    public ResponseEntity<?> getApplications(
            @Parameter(description = "Trạng thái (PENDING, REVIEWING, INTERVIEW, HIRED, REJECTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách đơn ứng tuyển");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết đơn ứng tuyển")
    public ResponseEntity<?> getApplicationDetail(
            @Parameter(description = "Application ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết đơn");
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "ApplicationTimeline", description = "Xem lịch sử trạng thái (Pending→Hired/Rejected)")
    public ResponseEntity<?> getApplicationTimeline(
            @Parameter(description = "Application ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Timeline đơn ứng tuyển");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Rút hồ sơ", description = "Chỉ rút khi trạng thái là Pending")
    public ResponseEntity<?> withdrawApplication(
            @Parameter(description = "Application ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Rút hồ sơ thành công");
    }

    @GetMapping("/cv/application/{id}")
    @Operation(summary = "Xem CV gắn theo đơn")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER')")
    public ResponseEntity<?> getApplicationCV(
            @Parameter(description = "Application ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("CV của đơn");
    }
}
