package com.yoedu.job_board_platform.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(
        @NotBlank(message = "Tên công ty không được để trống")
        @Size(max = 100, message = "Tên công ty không được quá 100 ký tự")
        String companyName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email sai định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        String password,

        @NotBlank(message = "Yêu cầu xác nhận mật khẩu")
        String confirmPassword,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
        String phone
) {
}
