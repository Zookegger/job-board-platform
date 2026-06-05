package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.BASE + "/saved-jobs")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Việc làm đã lưu", description = "Lưu, bỏ lưu và xem danh sách việc làm yêu thích. Yêu cầu role CANDIDATE.")
public class SavedJobController {

    @GetMapping
    @Operation(summary = "Danh sách việc đã lưu", description = """
            Lấy danh sách tất cả công việc mà ứng viên đã lưu (yêu thích).
            Kết quả phân trang, sắp xếp theo ngày lưu mới nhất.
            Mỗi item bao gồm thông tin tóm tắt của job.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sách việc làm đã lưu (có phân trang)", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ CANDIDATE)", content = @Content)
    })
    public ResponseEntity<?> getSavedJobs(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách việc đã lưu");
    }

    @PostMapping
    @Operation(summary = "Lưu việc làm", description = """
            Lưu một công việc vào danh sách yêu thích.
            Mỗi ứng viên chỉ được lưu một job một lần (không trùng).
            Dùng cho nút "Save Job" / "Bookmark" trên UI.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lưu việc làm thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Đã lưu công việc này trước đó hoặc jobId không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy công việc", content = @Content)
    })
    public ResponseEntity<?> saveJob(
            @Parameter(description = "ID của công việc cần lưu", example = "1", required = true)
            @RequestParam Long jobId
    ) {
        return ResponseEntity.ok("Lưu việc thành công");
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Bỏ lưu việc làm", description = "Xóa công việc khỏi danh sách yêu thích. Hành động này không thể hoàn tác — cần lưu lại nếu muốn thêm lại.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bỏ lưu việc làm thành công", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy công việc đã lưu hoặc chưa lưu job này", content = @Content)
    })
    public ResponseEntity<?> unsaveJob(
            @Parameter(description = "ID của công việc cần bỏ lưu", example = "1", required = true)
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok("Bỏ lưu thành công");
    }
}
