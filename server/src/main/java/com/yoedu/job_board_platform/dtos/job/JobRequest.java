package com.yoedu.job_board_platform.dtos.job;

import java.math.BigDecimal;
import java.util.Set;

import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.LocationTypes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobRequest(
                @Schema(description = "Tiêu đề công việc", example = "Senior Java Developer") @NotBlank @Size(max = 255) String title,

                @Schema(description = "Mô tả công việc") @NotBlank String description,

                @Schema(description = "Yêu cầu công việc") String requirements,
                @Schema(description = "Phúc lợi") String benefits,

                @Schema(description = "ID ngành nghề", example = "1") @NotNull Integer categoryId,

                @Schema(description = "Số lượng tuyển", example = "3") Integer numberOfOpenings,
                @Schema(description = "Lương tối thiểu", example = "20000000") BigDecimal salaryMin,
                @Schema(description = "Lương tối đa", example = "40000000") BigDecimal salaryMax,
                @Schema(description = "Loại tiền tệ", example = "VND") @Size(max = 10) String currency,
                @Schema(description = "Địa điểm", example = "123 Nguyễn Huệ, Quận 1") String location,

                @Schema(description = "Hình thức làm việc", example = "ONSITE") @NotNull LocationTypes locationTypes,

                @Schema(description = "Loại hình công việc", example = "FULL_TIME") @NotNull EmploymentType employmentType,

                @Schema(description = "Cấp bậc kinh nghiệm", example = "SENIOR") @NotNull ExperienceLevel experienceLevel,

                @Schema(description = "Danh sách ID kỹ năng", example = "[1, 2, 3]") Set<Integer> skillIds) {
        public JobRequest {
                if (description != null) {
                        description = description.replace("\\n", "\n");
                }
                if (requirements != null) {
                        requirements = requirements.replace("\\n", "\n");
                }
                if (benefits != null) {
                        benefits = benefits.replace("\\n", "\n");
                }
        }
}
