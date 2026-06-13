package com.yoedu.job_board_platform.controllers.api;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Trang tìm việc (Public)", description = "Danh sách việc, tìm kiếm, lọc - không cần đăng nhập")
public interface PublicJobApi {

    @Operation(summary = "Danh sách việc làm công khai", description = """
            Lấy danh sách việc làm đang Active (công khai) với phân trang và sắp xếp.
            Kết quả trả về gồm thông tin tóm tắt: tiêu đề, công ty, địa điểm, mức lương, ngày đăng.
            Không yêu cầu xác thực.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách việc có phân trang — mặc định 12 item/trang, sắp xếp theo ngày tạo", content = @Content),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content)
    })
    ResponseEntity<?> getJobs(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") int page,
            @Parameter(description = "Số lượng item trên mỗi trang", example = "12") int size,
            @Parameter(description = "Sắp xếp theo trường (date_created, updated_at)", example = "date_created") String sortBy);

    @Operation(summary = "Tìm kiếm nâng cao", description = """
            Tìm kiếm việc làm theo từ khóa, ngành nghề và địa điểm.
            Tất cả tham số đều không bắt buộc — nếu không có filter nào được cung cấp, trả về toàn bộ danh sách.
            Hỗ trợ full-text search trên tiêu đề và mô tả công việc.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kết quả tìm kiếm khớp với bộ lọc", content = @Content),
            @ApiResponse(responseCode = "400", description = "Tham số tìm kiếm không hợp lệ", content = @Content)
    })
    ResponseEntity<?> searchJobs(
            @Parameter(description = "Từ khóa tìm kiếm (tìm trong tiêu đề & mô tả)", example = "Java") String keyword,
            @Parameter(description = "ID ngành nghề (category)", example = "1") Long categoryId,
            @Parameter(description = "Địa điểm làm việc", example = "Hà Nội") String location);

    @Operation(summary = "Chi tiết công việc", description = """
            Lấy thông tin chi tiết của một công việc cụ thể bao gồm: mô tả, yêu cầu, phúc lợi,
            mức lương, loại hình, cấp bậc, thông tin công ty tuyển dụng.
            Chỉ trả về nếu job ở trạng thái Active.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chi tiết công việc + thông tin công ty", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy công việc hoặc không ở trạng thái Active", content = @Content)
    })
    ResponseEntity<?> getJobDetail(
            @Parameter(description = "ID của công việc cần xem chi tiết", example = "1", required = true) Long id);

    @Operation(summary = "Metadata cho bộ lọc", description = """
            Trả về danh sách các option filter: ngành nghề, địa điểm, mức lương, loại hình công việc, cấp bậc.
            Dùng để render dropdown/checkbox trên UI (JobFilterSidebar).
            Không yêu cầu xác thực.
            """)
    @ApiResponse(responseCode = "200", description = "Danh sách các option filter phân loại theo nhóm", content = @Content)
    ResponseEntity<?> getFilterOptions();

    @Operation(summary = "Danh sách ngành nghề", description = "Lấy tất cả danh mục ngành nghề đang có trong hệ thống. Dùng để hiển thị dropdown chọn ngành khi tìm kiếm hoặc đăng tin.")
    @ApiResponse(responseCode = "200", description = "Danh sách các ngành nghề (id + name)", content = @Content)
    ResponseEntity<?> getCategories();

    @Operation(summary = "Thông tin công ty (công khai)", description = """
            Lấy thông tin cơ bản của công ty: tên, địa chỉ, mô tả, website, logo, email, số điện thoại.
            Chỉ trả về nếu công ty đã được duyệt (isApproved = true).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin công khai của công ty", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty hoặc chưa được duyệt", content = @Content)
    })
    ResponseEntity<?> getCompanyInfo(
            @Parameter(description = "ID của công ty", example = "1", required = true) Long id);
}
