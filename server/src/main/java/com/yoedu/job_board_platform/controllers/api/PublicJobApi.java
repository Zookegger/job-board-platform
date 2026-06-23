package com.yoedu.job_board_platform.controllers.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Trang tìm vi?c (Public)", description = "Danh sach viec, tim kiem, loc - khong can dang nhap")
public interface PublicJobApi {

        @Operation(summary = "Danh sach viec lam cong khai")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Danh sach viec co phan trang", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Tham so khong hop le", content = @Content)
        })
        ResponseEntity<Page<JobListResponse>> getJobs(
                        @Parameter(description = "So trang (bat dau tu 0)", example = "0") int page,
                        @Parameter(description = "So luong item tren moi trang", example = "12") int size);

        @Operation(summary = "Chi tiet cong viec")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Chi tiet cong viec", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Khong tim thay cong viec hoac khong o trang thai Active", content = @Content)
        })
        ResponseEntity<JobResponse> getJobDetail(
                        @Parameter(description = "ID cua cong viec", required = true) UUID id);
}
