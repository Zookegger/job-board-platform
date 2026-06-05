package com.yoedu.job_board_platform.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(
        @Schema(description = "Tên công ty", example = "Yoedu Technology Corporation")
        @NotBlank(message = "Tên công ty không được để trống")
        @Size(max = 100, message = "Tên công ty không được quá 100 ký tự")
        String companyName,

        @Schema(description = "Email công ty", example = "hr@yoedu.com")
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email sai định dạng")
        String email,

        @Schema(description = "Mật khẩu", example = "password123")
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        String password,

        @Schema(description = "Xác nhận mật khẩu", example = "password123")
        @NotBlank(message = "Yêu cầu xác nhận mật khẩu")
        String confirmPassword,

        @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
        String phone
) {
}
