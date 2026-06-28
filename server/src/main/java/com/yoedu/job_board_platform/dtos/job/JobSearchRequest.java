package com.yoedu.job_board_platform.dtos.job;

import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.LocationTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

@Schema(description = "Bộ lọc tìm kiếm việc làm công khai")
public record JobSearchRequest(

        @Schema(description = "Từ khóa tìm kiếm (title + description)")
        @Size(max = 100, message = "Từ khóa không được vượt quá 100 ký tự")
        String keyword,

        @Schema(description = "Danh sách ID ngành nghề")
        @Size(max = 10, message = "Chỉ được chọn tối đa 10 ngành nghề cùng lúc")
        Set<@NotNull(message = "ID ngành nghề không được để trống") @Positive(message = "ID ngành nghề phải là số dương") Integer> categoryIds,

        @Schema(description = "Hình thức làm việc")
        @Size(max = 5, message = "Danh sách hình thức làm việc không hợp lệ")
        Set<@NotNull(message = "Hình thức làm việc không được để trống") LocationTypes> locationTypes,

        @Schema(description = "Loại hình công việc")
        @Size(max = 5, message = "Danh sách loại hình công việc không hợp lệ")
        Set<@NotNull(message = "Loại hình công việc không được để trống") EmploymentType> employmentTypes,

        @Schema(description = "Cấp bậc kinh nghiệm")
        @Size(max = 5, message = "Danh sách cấp bậc kinh nghiệm không hợp lệ")
        Set<@NotNull(message = "Cấp bậc kinh nghiệm không được để trống") ExperienceLevel> experienceLevels,

        @Schema(description = "Lương tối thiểu")
        @PositiveOrZero(message = "Mức lương tối thiểu không được là số âm")
        BigDecimal minSalary,

        @Schema(description = "Lương tối đa")
        @PositiveOrZero(message = "Mức lương tối đa không được là số âm")
        BigDecimal maxSalary,

        @Schema(description = "Danh sách ID kỹ năng yêu cầu")
        @Size(max = 20, message = "Chỉ được chọn tối đa 20 kỹ năng cùng lúc")
        Set<@NotNull(message = "ID kỹ năng không được để trống") @Positive(message = "ID kỹ năng phải là số dương") Integer> skillIds
) {
    // Logic check chéo giữa minSalary và maxSalary
    public JobSearchRequest {
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new IllegalArgumentException("Mức lương tối thiểu không được lớn hơn mức lương tối đa");
        }
    }
}