package com.yoedu.job_board_platform.controllers.api;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Danh mục ngành nghề", description = "Quản lý ngành nghề: danh sách, thêm, sửa, xóa.")
public interface JobCategoryApi {

    @Operation(summary = "Danh sách ngành nghề", description = "Lấy tất cả danh mục ngành nghề (công khai, không yêu cầu xác thực).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Danh sách ngành nghề (id + name)", content = @Content)
    ResponseEntity<List<JobCategoryResponse>> getCategories();

    @Operation(summary = "Thêm ngành nghề mới", description = "Tạo ngành nghề mới. Yêu cầu role ADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tên ngành đã tồn tại hoặc dữ liệu không hợp lệ", content = @Content)
    })
    ResponseEntity<JobCategoryResponse> createCategory(
            @Parameter(description = "Thông tin ngành nghề mới", required = true) JobCategoryRequest request);

    @Operation(summary = "Cập nhật ngành nghề", description = "Chỉnh sửa tên ngành nghề. Yêu cầu role ADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy ngành nghề", content = @Content)
    })
    ResponseEntity<JobCategoryResponse> updateCategory(
            @Parameter(description = "ID ngành nghề", required = true) Integer id,
            @Parameter(description = "Thông tin cập nhật", required = true) JobCategoryRequest request);

    @Operation(summary = "Xóa ngành nghề", description = "Xóa ngành nghề. Yêu cầu role ADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ngành đang có công việc liên kết — không thể xóa", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy ngành nghề", content = @Content)
    })
    ResponseEntity<ApiResponse> deleteCategory(
            @Parameter(description = "ID ngành nghề cần xóa", required = true) Integer id);
}
