package com.yoedu.job_board_platform.services.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.mappers.ProfileMapper;
import com.yoedu.job_board_platform.models.CandidateDetail;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.ProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final CandidateDetailRepository candidateDetailRepository;
    private final CompanyEmployerDetailRepository companyEmployerDetailRepository;
    private final CompanyRepository companyRepository;

    @Override
    public CandidateProfileResponse getCurrentCandidateProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new ForbiddenException("Chỉ ứng viên mới có thể xem hồ sơ ứng viên");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));
        return profileMapper.toCandidateResponse(profile);
    }

    @Override
    public EmployerProfileResponse getCurrentEmployerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
        if (user.getRole() != UserRole.EMPLOYER) {
            throw new ForbiddenException("Chỉ nhà tuyển dụng mới có thể xem hồ sơ nhà tuyển dụng");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));
        return profileMapper.toEmployerResponse(profile);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateCurrentCandidateProfile(CandidateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new ForbiddenException("Chỉ ứng viên mới có thể cập nhật hồ sơ ứng viên");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        profileMapper.updateCandidateEntity(request, profile);
        profileRepository.save(profile);

        return profileMapper.toCandidateResponse(profile);
    }

    @Override
    @Transactional
    public EmployerProfileResponse updateCurrentEmployerProfile(EmployerProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
        if (user.getRole() != UserRole.EMPLOYER) {
            throw new ForbiddenException("Chỉ nhà tuyển dụng mới có thể cập nhật hồ sơ nhà tuyển dụng");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        profileMapper.updateEmployerEntity(request, profile);
        profileRepository.save(profile);

        CompanyEmployerDetail employerDetail = companyEmployerDetailRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin nhà tuyển dụng"));

        if (request.roleInCompany() != null) {
            employerDetail.setRoleInCompany(request.roleInCompany());
            companyEmployerDetailRepository.save(employerDetail);
        }

        Company company = employerDetail.getCompany();
        boolean companyChanged = false;
        if (request.companyName() != null) { company.setCompanyName(request.companyName()); companyChanged = true; }
        if (request.address() != null) { company.setAddress(request.address()); companyChanged = true; }
        if (request.description() != null) { company.setDescription(request.description()); companyChanged = true; }
        if (request.website() != null) { company.setWebsite(request.website()); companyChanged = true; }
        if (request.logoUrl() != null) { company.setLogoUrl(request.logoUrl()); companyChanged = true; }
        if (companyChanged) {
            companyRepository.save(company);
        }

        return profileMapper.toEmployerResponse(profile);
    }

    @Override
    @Transactional
    public Profile createProfile(User user, String fullName, String phone, String avatarUrl) {
        Profile profile = Profile.builder()
                .user(user)
                .fullName(fullName)
                .phone(phone != null ? phone : "")
                .avatarUrl(avatarUrl)
                .build();
        return profileRepository.save(profile);
    }
}
