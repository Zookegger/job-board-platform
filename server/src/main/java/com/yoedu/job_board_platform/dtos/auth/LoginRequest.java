package com.yoedu.job_board_platform.dtos.auth;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Email đăng nhập", example = "user@example.com")
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @Schema(description = "Mật khẩu", example = "password123")
        @NotBlank(message = "Mật khẩu không được để trống")
        @Length(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
        String password
) {}
