package com.yoedu.job_board_platform.dtos.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String role,
        boolean isActive,
        String fullName
) {
}
