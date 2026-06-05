package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping(ApiPaths.BASE + "/public")
@Tag(name = "Trang tìm việc (Public)", description = "Danh sách việc, tìm kiếm, lọc - không cần đăng nhập")
public class PublicJobController {

    @GetMapping("/jobs")
    @Operation(summary = "Danh sách việc làm công khai", description = """
            Lấy danh sách việc làm đang Active (công khai) với phân trang và sắp xếp.
            Kết quả trả về gồm thông tin tóm tắt: tiêu đề, công ty, địa điểm, mức lương, ngày đăng.
            Không yêu cầu xác thực.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sách việc có phân trang — mặc định 12 item/trang, sắp xếp theo ngày tạo", content = @Content),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content)
    })
    public ResponseEntity<?> getJobs(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trên mỗi trang", example = "12")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Sắp xếp theo trường (date_created, updated_at)", example = "date_created")
            @RequestParam(defaultValue = "date_created") String sortBy
    ) {
        return ResponseEntity.ok("Danh sách việc");
    }

    @GetMapping("/jobs/search")
    @Operation(summary = "Tìm kiếm nâng cao", description = """
            Tìm kiếm việc làm theo từ khóa, ngành nghề và địa điểm.
            Tất cả tham số đều không bắt buộc — nếu không có filter nào được cung cấp, trả về toàn bộ danh sách.
            Hỗ trợ full-text search trên tiêu đề và mô tả công việc.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kết quả tìm kiếm khớp với bộ lọc", content = @Content),
        @ApiResponse(responseCode = "400", description = "Tham số tìm kiếm không hợp lệ", content = @Content)
    })
    public ResponseEntity<?> searchJobs(
            @Parameter(description = "Từ khóa tìm kiếm (tìm trong tiêu đề & mô tả)", example = "Java")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "ID ngành nghề (category)", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Địa điểm làm việc", example = "Hà Nội")
            @RequestParam(required = false) String location
    ) {
        return ResponseEntity.ok("Kết quả tìm kiếm");
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Chi tiết công việc", description = """
            Lấy thông tin chi tiết của một công việc cụ thể bao gồm: mô tả, yêu cầu, phúc lợi,
            mức lương, loại hình, cấp bậc, thông tin công ty tuyển dụng.
            Chỉ trả về nếu job ở trạng thái Active.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chi tiết công việc + thông tin công ty", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy công việc hoặc không ở trạng thái Active", content = @Content)
    })
    public ResponseEntity<?> getJobDetail(
            @Parameter(description = "ID của công việc cần xem chi tiết", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết job");
    }

    @GetMapping("/jobs/filter-options")
    @Operation(summary = "Metadata cho bộ lọc", description = """
            Trả về danh sách các option filter: ngành nghề, địa điểm, mức lương, loại hình công việc, cấp bậc.
            Dùng để render dropdown/checkbox trên UI (JobFilterSidebar).
            Không yêu cầu xác thực.
            """)
    @ApiResponse(responseCode = "200", description = "Danh sách các option filter phân loại theo nhóm", content = @Content)
    public ResponseEntity<?> getFilterOptions() {
        return ResponseEntity.ok("Filter options");
    }

    @GetMapping("/categories")
    @Operation(summary = "Danh sách ngành nghề", description = "Lấy tất cả danh mục ngành nghề đang có trong hệ thống. Dùng để hiển thị dropdown chọn ngành khi tìm kiếm hoặc đăng tin.")
    @ApiResponse(responseCode = "200", description = "Danh sách các ngành nghề (id + name)", content = @Content)
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sách ngành");
    }

    @GetMapping("/companies/{id}")
    @Operation(summary = "Thông tin công ty (công khai)", description = """
            Lấy thông tin cơ bản của công ty: tên, địa chỉ, mô tả, website, logo, email, số điện thoại.
            Chỉ trả về nếu công ty đã được duyệt (isApproved = true).
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thông tin công khai của công ty", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty hoặc chưa được duyệt", content = @Content)
    })
    public ResponseEntity<?> getCompanyInfo(
            @Parameter(description = "ID của công ty", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Thông tin công ty");
    }
}
