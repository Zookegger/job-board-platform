package com.yoedu.job_board_platform.dtos.user;

import com.yoedu.job_board_platform.models.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 255) String password,
        @NotNull UserRole role,
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Size(max = 15) String phone,
        @Size(max = 2048) String avatarUrl
) {
}
