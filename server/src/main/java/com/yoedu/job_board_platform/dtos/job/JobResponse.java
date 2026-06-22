package com.yoedu.job_board_platform.dtos.job;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobResponse(
                @Schema(description = "ID tin tuyển dụng") UUID id,
                @Schema(description = "Tiêu đề công việc") String title,
                @Schema(description = "Slug") String slug,
                @Schema(description = "Mô tả công việc") String description,
                @Schema(description = "Yêu cầu công việc") String requirements,
                @Schema(description = "Phúc lợi") String benefits,
                @Schema(description = "Số lượng tuyển") Integer numberOfOpenings,
                @Schema(description = "Lương tối thiểu") BigDecimal salaryMin,
                @Schema(description = "Lương tối đa") BigDecimal salaryMax,
                @Schema(description = "Loại tiền tệ") String currency,
                @Schema(description = "Địa điểm") String location,
                @Schema(description = "Hình thức làm việc") LocationTypes locationTypes,
                @Schema(description = "Loại hình công việc") EmploymentType employmentType,
                @Schema(description = "Cấp bậc kinh nghiệm") ExperienceLevel experienceLevel,
                @Schema(description = "Trạng thái") JobStatus status,
                @Schema(description = "Ngày đăng") OffsetDateTime postedDate,
                @Schema(description = "Ngày hết hạn") OffsetDateTime expirationDate,
                @Schema(description = "Ngày tạo") OffsetDateTime createdAt,
                @Schema(description = "Ngày cập nhật") OffsetDateTime updatedAt,
                @Schema(description = "ID công ty") UUID companyId,
                @Schema(description = "Tên công ty") String companyName,
                @Schema(description = "ID ngành nghề") Integer categoryId,
                @Schema(description = "Tên ngành nghề") String categoryName,
                @Schema(description = "Danh sách kỹ năng") List<SkillResponse> skills) {

        public JobResponse withSkills(List<SkillResponse> skills) {
                return new JobResponse(
                                id, title, slug, description, requirements, benefits,
                                numberOfOpenings, salaryMin, salaryMax, currency,
                                location, locationTypes, employmentType, experienceLevel,
                                status, postedDate, expirationDate, createdAt, updatedAt,
                                companyId, companyName, categoryId, categoryName,
                                skills);
        }
}
