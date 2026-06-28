package com.yoedu.job_board_platform.controllers.api;

import com.yoedu.job_board_platform.dtos.company.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name = "Công ty", description = "Xem và cập nhật thông tin công ty. Hỗ trợ cả nhà tuyển dụng và ứng viên (xem thông tin công ty từ tin tuyển dụng).")
public interface CompanyApi {

    @Operation(summary = "Thông tin công ty của tôi", description = "Lấy thông tin chi tiết công ty mà nhà tuyển dụng hiện tại đang quản lý. Yêu cầu role EMPLOYER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin công ty của employer hiện tại", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa có thông tin công ty", content = @Content)
    })
    ResponseEntity<CompanyResponse> findCompanyByEmployerId();

    @Operation(summary = "Cập nhật thông tin công ty", description = "Cập nhật thông tin công ty của nhà tuyển dụng hiện tại: tên, địa chỉ, mô tả, website, logo, email, số điện thoại. Các trường null được bỏ qua.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thông tin công ty thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    ResponseEntity<CompanyResponse> update(
            @Parameter(description = "Thông tin công ty cần cập nhật", required = true) CompanyRequest request);

    @Operation(summary = "Công ty theo tin tuyển dụng", description = "Lấy thông tin công ty dựa trên ID của tin tuyển dụng. API công khai — không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin công ty", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng hoặc công ty", content = @Content)
    })
    ResponseEntity<CompanyResponse> getCompanyByJobPost(
            @Parameter(description = "ID của tin tuyển dụng", example = "550e8400-e29b-41d4-a716-446655440000", required = true) UUID jobPostId);

    @Operation(summary = "Danh sách công ty", description = "Lấy danh sách tất cả các công ty trên hệ thống. API công khai — không yêu cầu đăng nhập.")
    @ApiResponse(responseCode = "200", description = "Danh sách công ty", content = @Content)
    ResponseEntity<List<CompanyResponse>> listCompanies();

    @Operation(summary = "Danh sách công ty", description = "Lấy danh sách công ty có hỗ trợ tìm kiếm theo từ khóa, lọc theo ngành nghề và phân trang.")
    @ApiResponse(responseCode = "200", description = "Danh sách công ty", content = @Content)
    ResponseEntity<Page<PublicCompanyResponse>> listCompaniesPage(
            @Parameter(description = "Từ khóa tìm kiếm theo tên công ty", required = false, example = "Tech") String keyword,

            @Parameter(description = "Danh sách ID ngành nghề để lọc công ty theo lĩnh vực hoạt động", required = false, example = "1,2,3") Set<Integer> jobCategoryIds,

            @Parameter(description = "Thông tin phân trang và sắp xếp (page, size, sort). Ví dụ: page=0&size=10&sort=createdAt,desc", required = false, example = "page=0&size=10&sort=createdAt,desc") Pageable pageable);

    @Operation(summary = "Trạng thái phê duyệt công ty", description = "Lấy trạng thái phê duyệt hiện tại của công ty mà employer đang quản lý. Yêu cầu role EMPLOYER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trạng thái phê duyệt công ty", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa có thông tin công ty", content = @Content)
    })
    ResponseEntity<CompanyStatusResponse> getStatus();

    /**
     * Lấy lịch sử phê duyệt của công ty, sắp xếp mới nhất lên đầu.
     * Yêu cầu role EMPLOYER.
     *
     * @return danh sách ApprovalLogResponse
     */
    @Operation(summary = "Lịch sử phê duyệt công ty", description = "Lấy danh sách lịch sử thay đổi trạng thái phê duyệt của công ty, sắp xếp mới nhất lên đầu. Yêu cầu role EMPLOYER.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách lịch sử phê duyệt", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa có thông tin công ty", content = @Content)
    })
    ResponseEntity<List<ApprovalLogResponse>> getApprovalHistory();
}
