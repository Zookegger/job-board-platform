package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.models.User;

/**
 * Service xác thực và đăng ký người dùng.
 * Xử lý đăng nhập, refresh token, đăng ký ứng viên/nhà tuyển dụng,
 * đăng xuất và trích xuất thông tin người dùng từ token.
 */
public interface AuthService {
    AuthResult authenticate(String email, String password);

    AuthResult refreshToken(String tokenString);

    void registerCandidate(CandidateRegisterRequest request);

    void registerCompany(CompanyRegisterRequest request);

    void logout(String refreshToken);

    UUID extractUserId(String accessToken);

    User getCurrentUser();
}
