package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Hồ sơ ứng viên", description = "Quản lý thông tin cá nhân, CV - chỉ CANDIDATE")
public class CandidateProfileController {

    @GetMapping
    @Operation(summary = "Lấy thông tin cá nhân")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok("Thông tin profile");
    }

    @PutMapping
    @Operation(summary = "Cập nhật hồ sơ + avatar")
    public ResponseEntity<?> updateProfile() {
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @PostMapping("/cv")
    @Operation(summary = "Upload PDF CV", description = "CVUploader - multipart form")
    public ResponseEntity<?> uploadCV(
            @Parameter(description = "File PDF")
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok("Upload thành công");
    }

    @GetMapping("/cv")
    @Operation(summary = "Xem / tải CV PDF đã lưu")
    public ResponseEntity<?> downloadCV() {
        return ResponseEntity.ok("PDF CV");
    }

    @GetMapping("/resumes")
    @Operation(summary = "Danh sách CV online đã tạo")
    public ResponseEntity<?> getResumes() {
        return ResponseEntity.ok("Danh sách resume");
    }

    @PostMapping("/resumes")
    @Operation(summary = "Tạo CV online mới")
    public ResponseEntity<?> createResume() {
        return ResponseEntity.ok("Tạo resume thành công");
    }

    @PutMapping("/resumes/{id}")
    @Operation(summary = "Chỉnh sửa CV online")
    public ResponseEntity<?> updateResume(
            @Parameter(description = "Resume ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Cập nhật resume thành công");
    }

    @DeleteMapping("/resumes/{id}")
    @Operation(summary = "Xóa CV online")
    public ResponseEntity<?> deleteResume(
            @Parameter(description = "Resume ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Xóa resume thành công");
    }
}
