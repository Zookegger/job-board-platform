package com.yoedu.job_board_platform.services;

import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;

/**
 * Service quản lý hồ sơ người dùng (Profile).
 * Hỗ trợ xem/cập nhật hồ sơ cho ứng viên và nhà tuyển dụng,
 * tạo hồ sơ mới và upload avatar.
 */
public interface ProfileService {
    CandidateProfileResponse getCurrentCandidateProfile();
    EmployerProfileResponse getCurrentEmployerProfile();
    CandidateProfileResponse updateCurrentCandidateProfile(CandidateProfileRequest request);
    EmployerProfileResponse updateCurrentEmployerProfile(EmployerProfileRequest request);
    Profile createProfile(User user, String fullName, String phone, String avatarUrl);
    String uploadAvatar(MultipartFile file);
    String uploadCompanyLogo(MultipartFile file);
}
