package com.yoedu.job_board_platform.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateRegisterRequest(
        @Schema(description = "Email ứng viên", example = "candidate@example.com")
        @NotBlank(message = "Email không được để trống") @Email(message = "Email sai định dạng") String email,

        @Schema(description = "Họ và tên ứng viên", example = "Nguyễn Văn A")
        @NotBlank(message = "Họ tên không được để trống") String fullName,

        @Schema(description = "Mật khẩu", example = "password123")
        @NotBlank(message = "Password không được để trống") @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") String password,

        @Schema(description = "Xác nhận mật khẩu", example = "password123")
        @NotBlank(message = "Yêu cầu xác nhận mật khẩu") String confirmPassword) {
}
