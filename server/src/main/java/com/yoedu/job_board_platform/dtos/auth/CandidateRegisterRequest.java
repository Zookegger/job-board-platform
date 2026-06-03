package com.yoedu.job_board_platform.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateRegisterRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email sai định dạng") String email,

        @NotBlank(message = "Họ tên không được để trống") String fullName,

        @NotBlank(message = "Password không được để trống") @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") String password,

        @NotBlank(message = "Yêu cầu xác nhận mật khẩu") String confirmPassword) {
}