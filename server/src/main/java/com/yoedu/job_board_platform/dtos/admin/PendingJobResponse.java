package com.yoedu.job_board_platform.dtos.admin;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;

import io.swagger.v3.oas.annotations.media.Schema;

public record PendingJobResponse(
        @Schema(description = "ID tin tuyển dụng") UUID id,
        @Schema(description = "Tiêu đề công việc") String title,
        @Schema(description = "Trạng thái") JobStatus status,
        @Schema(description = "Mô tả công việc") String description,
        @Schema(description = "Yêu cầu ứng viên") String requirements,
        @Schema(description = "Quyền lợi") String benefits,
        @Schema(description = "Địa điểm") String location,
        @Schema(description = "Hình thức làm việc") LocationTypes locationTypes,
        @Schema(description = "Loại hình công việc") EmploymentType employmentType,
        @Schema(description = "Cấp bậc kinh nghiệm") ExperienceLevel experienceLevel,
        @Schema(description = "Lương tối thiểu") BigDecimal salaryMin,
        @Schema(description = "Lương tối đa") BigDecimal salaryMax,
        @Schema(description = "Loại tiền tệ") String currency,
        @Schema(description = "Số lượng tuyển") Integer numberOfOpenings,
        @Schema(description = "Tên công ty") String companyName,
        @Schema(description = "Logo công ty") String companyLogoUrl,
        @Schema(description = "Tên ngành nghề") String categoryName,
        @Schema(description = "Ngày tạo") OffsetDateTime createdAt) {
}
