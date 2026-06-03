package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;

public interface AuthService {
    AuthResult authenticate(String email, String password);
    AuthResult refreshToken(String tokenString);
    
    void registerCandidate(CandidateRegisterRequest request);
    void registerCompany(CompanyRegisterRequest request);
    void logout(String refreshToken);
    UUID extractUserId(String accessToken);
    
}
