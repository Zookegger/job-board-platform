package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;

public interface ProfileService {
    CandidateProfileResponse getCurrentCandidateProfile();
    EmployerProfileResponse getCurrentEmployerProfile();
    CandidateProfileResponse updateCurrentCandidateProfile(CandidateProfileRequest request);
    EmployerProfileResponse updateCurrentEmployerProfile(EmployerProfileRequest request);
    Profile createProfile(User user, String fullName, String phone, String avatarUrl);
}
