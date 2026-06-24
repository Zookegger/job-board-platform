package com.yoedu.job_board_platform.dtos.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobCategoryRequest(
    @NotBlank(message = "Tên ngành nghề không được để trống")
    @Size(max = 100, message = "Tên ngành nghề tối đa 100 ký tự")
    String name
) {}
