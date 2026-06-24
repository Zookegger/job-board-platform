package com.yoedu.job_board_platform.dtos.company;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thông tin công ty trong danh sách (công khai)")
public record PublicCompanyListResponse(

        @Schema(description = "Tên công ty", example = "YoEdu Technology") String name,

        @Schema(description = "Slug định danh công ty dùng cho URL", example = "yoedu-technology") String slug,

        @Schema(description = "URL logo công ty", example = "https://cdn.example.com/logos/yoedu.png") String logoUrl,

        @Schema(description = "Mô tả ngắn về công ty", example = "Công ty công nghệ chuyên về nền tảng giáo dục trực tuyến") String description,

        @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Quận 1, TP.HCM") String address,

        @Schema(description = "Website chính thức của công ty", example = "https://yoedu.vn") String website,

        @Schema(description = "Tổng số tin tuyển dụng đang mở của công ty", example = "12") Long totalOpenJobs,

        @Schema(description = "Danh sách ngành nghề công ty đang tuyển dụng") List<JobCategoryResponse> categories) {
}
