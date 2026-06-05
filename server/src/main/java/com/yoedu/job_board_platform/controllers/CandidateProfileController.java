package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Hồ sơ ứng viên", description = "Quản lý thông tin cá nhân, upload CV (PDF), tạo CV online. Yêu cầu role CANDIDATE.")
public class CandidateProfileController {

    @GetMapping
    @Operation(summary = "Hồ sơ cá nhân", description = """
            Lấy thông tin hồ sơ cá nhân của ứng viên đang đăng nhập: 
            họ tên, email, số điện thoại, địa chỉ, avatar, kỹ năng...
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thông tin hồ sơ cá nhân", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ CANDIDATE)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chưa có hồ sơ", content = @Content)
    })
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok("Thông tin profile");
    }

    @PutMapping
    @Operation(summary = "Cập nhật hồ sơ", description = """
            Cập nhật thông tin hồ sơ cá nhân: họ tên, số điện thoại, địa chỉ, 
            kỹ năng, kinh nghiệm, học vấn... Có thể upload ảnh avatar mới.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật hồ sơ thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<?> updateProfile() {
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @PostMapping("/cv")
    @Operation(summary = "Upload CV (PDF)", description = """
            Upload file CV PDF của ứng viên.
            File sẽ được lưu trữ và gắn với hồ sơ của ứng viên.
            Định dạng hỗ trợ: PDF. Dung lượng tối đa: 10MB.
            Dùng cho component CVUploader.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upload CV thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "File không hợp lệ (sai định dạng, quá dung lượng)", content = @Content)
    })
    public ResponseEntity<?> uploadCV(
            @Parameter(description = "File PDF CV (multipart/form-data, max 10MB)", required = true)
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok("Upload thành công");
    }

    @GetMapping("/cv")
    @Operation(summary = "Xem/tải CV (PDF)", description = "Tải file CV PDF đã upload của ứng viên hiện tại.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File PDF CV", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
    })
    public ResponseEntity<?> downloadCV() {
        return ResponseEntity.ok("PDF CV");
    }

    @GetMapping("/resumes")
    @Operation(summary = "Danh sách CV online", description = "Lấy danh sách tất cả CV online mà ứng viên đã tạo. Mỗi CV gồm tiêu đề, ngày tạo, trạng thái.")
    @ApiResponse(responseCode = "200", description = "Danh sách CV online", content = @Content)
    public ResponseEntity<?> getResumes() {
        return ResponseEntity.ok("Danh sách resume");
    }

    @PostMapping("/resumes")
    @Operation(summary = "Tạo CV online mới", description = "Tạo một CV online mới với đầy đủ thông tin: kinh nghiệm làm việc, học vấn, kỹ năng, chứng chỉ, dự án...")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo CV online thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content)
    })
    public ResponseEntity<?> createResume() {
        return ResponseEntity.ok("Tạo resume thành công");
    }

    @PutMapping("/resumes/{id}")
    @Operation(summary = "Cập nhật CV online", description = "Chỉnh sửa thông tin CV online đã tạo: cập nhật kinh nghiệm, kỹ năng, học vấn...")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật CV online thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy CV", content = @Content)
    })
    public ResponseEntity<?> updateResume(
            @Parameter(description = "ID của CV online cần sửa", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Cập nhật resume thành công");
    }

    @DeleteMapping("/resumes/{id}")
    @Operation(summary = "Xóa CV online", description = "Xóa vĩnh viễn CV online khỏi hệ thống. Hành động này không thể hoàn tác.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Xóa CV online thành công", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy CV", content = @Content)
    })
    public ResponseEntity<?> deleteResume(
            @Parameter(description = "ID của CV online cần xóa", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Xóa resume thành công");
    }
}
