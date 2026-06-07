package com.yoedu.job_board_platform.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(
        // =========================
        // Thông tin công ty
        // =========================

        @Schema(description = "Tên công ty", example = "Yoedu Technology Corporation")
        @NotBlank(message = "Tên công ty không được để trống")
        @Size(max = 100, message = "Tên công ty không được quá 100 ký tự")
        String companyName,

        @Schema(description = "Mã số thuế", example = "0123456789")
        @NotBlank(message = "Mã số thuế không được để trống")
        @Size(max = 20, message = "Mã số thuế không được quá 20 ký tự")
        String taxCode,

        @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
        @NotBlank(message = "Địa chỉ không được để trống")
        String address,

        // =========================
        // Thông tin người đại diện
        // TODO: email + companyPhone đã được chuyển sang form cập nhật trong Employer Dashboard
        // =========================

        @Schema(description = "Đại diện HR công ty", example = "Nguyễn Văn A")
        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        @Schema(description = "Email của đại diện HR", example = "recruiter.nguyenvana@yoedu.com")
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email sai định dạng")
        String userEmail,

        @Schema(description = "Số điện thoại liên hệ đại diện HR", example = "0901234567")
        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 15, message = "Số điện thoại không được quá 15 ký tự")
        String userPhone,

        @Schema(description = "Mật khẩu", example = "password123")
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
        String password,

        @Schema(description = "Xác nhận mật khẩu", example = "password123")
        @NotBlank(message = "Yêu cầu xác nhận mật khẩu")
        String confirmPassword
) {
}
