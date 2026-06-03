package com.yoedu.job_board_platform.dtos.user;

import com.yoedu.job_board_platform.models.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email @Size(max = 255) String email,
        @Size(min = 8, max = 255) String password,
        @NotNull UserRole role,
        @Size(max = 100) String fullName,
        @Size(max = 15) String phone,
        @Size(max = 2048) String avatarUrl
) {
}
