package com.yoedu.job_board_platform.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Trang tìm việc (Public)", description = "Danh sách việc, tìm kiếm, lọc - không cần đăng nhập")
public class PublicJobController {

    @GetMapping("/jobs")
    @Operation(summary = "Danh sách việc + lọc/phân trang", description = "Lấy danh sách tất cả việc công khai với phân trang và bộ lọc")
    @ApiResponse(responseCode = "200", description = "Trả về danh sách việc")
    public ResponseEntity<?> getJobs(
            @Parameter(description = "Trang (từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item/trang")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Sắp xếp theo (date_created, updated_at)")
            @RequestParam(defaultValue = "date_created") String sortBy
    ) {
        return ResponseEntity.ok("Danh sách việc");
    }

    @GetMapping("/jobs/search")
    @Operation(summary = "Tìm nâng cao", description = "Tìm kiếm việc theo từ khóa, ngành, địa điểm")
    @ApiResponse(responseCode = "200", description = "Trả về kết quả tìm kiếm")
    public ResponseEntity<?> searchJobs(
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "ID ngành nghề")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Địa điểm")
            @RequestParam(required = false) String location
    ) {
        return ResponseEntity.ok("Kết quả tìm kiếm");
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Chi tiết job + thông tin công ty")
    @ApiResponse(responseCode = "200", description = "Trả về chi tiết job")
    @ApiResponse(responseCode = "404", description = "Job không tìm thấy")
    public ResponseEntity<?> getJobDetail(
            @Parameter(description = "Job ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết job");
    }

    @GetMapping("/jobs/filter-options")
    @Operation(summary = "Metadata cho JobFilterSidebar", description = "Trả về danh sách ngành, địa điểm... để UI lấy hiển thị filter")
    @ApiResponse(responseCode = "200", description = "Trả về filter metadata")
    public ResponseEntity<?> getFilterOptions() {
        return ResponseEntity.ok("Filter options");
    }

    @GetMapping("/categories")
    @Operation(summary = "Danh sách ngành nghề")
    @ApiResponse(responseCode = "200", description = "Trả về tất cả ngành")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sách ngành");
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Thông tin công ty công khai")
    @ApiResponse(responseCode = "200", description = "Trả về info công ty")
    @ApiResponse(responseCode = "404", description = "Công ty không tìm thấy")
    public ResponseEntity<?> getCompanyInfo(
            @Parameter(description = "Company ID")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Thông tin công ty");
    }
}
