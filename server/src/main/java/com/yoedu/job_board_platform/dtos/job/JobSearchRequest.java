package com.yoedu.job_board_platform.dtos.job;

import java.math.BigDecimal;
import java.util.Set;

import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.LocationTypes;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bộ lọc tìm kiếm việc làm công khai")
public record JobSearchRequest(
        @Schema(description = "Từ khóa tìm kiếm (title + description)") String keyword,

        @Schema(description = "Danh sách ID ngành nghề") Set<Integer> categoryIds,

        @Schema(description = "Hình thức làm việc") Set<LocationTypes> locationTypes,

        @Schema(description = "Loại hình công việc") Set<EmploymentType> employmentTypes,

        @Schema(description = "Cấp bậc kinh nghiệm") Set<ExperienceLevel> experienceLevels,

        @Schema(description = "Lương tối thiểu") BigDecimal minSalary,

        @Schema(description = "Lương tối đa") BigDecimal maxSalary,

        @Schema(description = "Danh sách ID kỹ năng yêu cầu") Set<Integer> skillIds) {
}