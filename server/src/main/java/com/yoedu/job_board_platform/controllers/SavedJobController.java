package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-jobs")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "🔖 Việc làm đã lưu", description = "Quản lý danh sách việc yêu thích - chỉ CANDIDATE")
public class SavedJobController {

    @GetMapping
    @Operation(summary = "Danh sách việc đã lưu")
    public ResponseEntity<?> getSavedJobs(
            @Parameter(description = "Trang")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách việc đã lưu");
    }

    @PostMapping
    @Operation(summary = "Lưu việc làm")
    public ResponseEntity<?> saveJob(
            @Parameter(description = "Job ID")
            @RequestParam Long jobId
    ) {
        return ResponseEntity.ok("Lưu việc thành công");
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Bỏ lưu việc làm")
    public ResponseEntity<?> unsaveJob(
            @Parameter(description = "Job ID")
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok("Bỏ lưu thành công");
    }
}
