package com.yoedu.job_board_platform.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.mappers.ApplicationMapper;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final SecurityUtil securityUtil;
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    private Profile getAuthorizedCandidateProfile(UUID candidateId) {
        securityUtil.isAuthorized(candidateId, List.of(UserRole.CANDIDATE));
        User user = securityUtil.getCurrentUser();

        if (user.getProfile() == null) {
            throw new ForbiddenException("Không tìm thấy hồ sơ ứng viên");
        }

        return user.getProfile();
    }

    @Override
    public Page<ApplicationListResponse> getCandidateApplications(
            UUID candidateId, ApplicationStatus status, int page, int size) {
        Profile profile = getAuthorizedCandidateProfile(candidateId);

        int safeSize = size > 0 ? Math.min(size, 100) : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(
                Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "appliedAt"));

        Page<Application> applications;
        if (status != null) {
            applications = applicationRepository.findByCandidate_IdAndStatus(
                    profile.getId(), status, pageable);
        } else {
            applications = applicationRepository.findByCandidate_Id(profile.getId(), pageable);
        }

        return applications.map(applicationMapper::toListResponse);
    }
}
