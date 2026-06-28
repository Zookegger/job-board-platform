package com.yoedu.job_board_platform.controllers.api;

import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.dtos.job.JobSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Việc làm (Jobs)", description = "API công khai cho tin tuyển dụng")
public interface JobApi {
	@Operation(summary = "Tìm kiếm việc làm công khai", description = "Tìm kiếm và lọc việc làm với nhiều tiêu chí. Chỉ trả về các job ACTIVE.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Kết quả tìm kiếm phân trang", content = @Content),
			@ApiResponse(responseCode = "400", description = "Tham số không hợp lệ", content = @Content)
	})
	ResponseEntity<Page<JobListResponse>> searchPublicJobs(
			@ParameterObject JobSearchRequest request,
			@ParameterObject Pageable pageable);

	@Operation(summary = "Chi tiết việc làm", description = "Lấy thông tin chi tiết của một công việc theo slug.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Chi tiết công việc", content = @Content),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy", content = @Content)
	})
	ResponseEntity<JobResponse> getPublicJobDetail(
			@Parameter(description = "Slug của công việc", required = true) String slug);

	@Operation(summary = "Việc làm tương tự", description = "Lấy danh sách việc làm liên quan dựa trên cùng ngành nghề và kỹ năng.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Danh sách việc làm tương tự", content = @Content),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy công việc gốc", content = @Content)
	})
	ResponseEntity<Page<JobListResponse>> getRelatedJobs(
			@Parameter(description = "ID của công việc", required = true) UUID id,
			@ParameterObject Pageable pageable);
}
