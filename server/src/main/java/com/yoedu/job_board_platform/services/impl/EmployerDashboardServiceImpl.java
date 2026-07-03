package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.dtos.employer.EmployerDashboardStatsResponse;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.services.EmployerDashboardService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployerDashboardServiceImpl implements EmployerDashboardService {

    private final SecurityUtil securityUtil;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    private Company getAuthorizedEmployerCompany(UUID employerId) {
        securityUtil.isAuthorized(employerId, List.of(UserRole.EMPLOYER));
        var user = securityUtil.getCurrentUser();

        if (user.getProfile() == null ||
                user.getProfile().getEmployerDetail() == null ||
                user.getProfile().getEmployerDetail().getCompany() == null) {
            throw new ForbiddenException("Không tìm thấy thông tin công ty của nhà tuyển dụng");
        }

        return user.getProfile().getEmployerDetail().getCompany();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerDashboardStatsResponse getStats(UUID employerId) {
        Company company = getAuthorizedEmployerCompany(employerId);
        UUID companyId = company.getId();
        OffsetDateTime weekAgo = OffsetDateTime.now().minusDays(7);

        long activeJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.ACTIVE);
        long pendingApprovalJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.PENDING_APPROVAL);
        long draftJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.DRAFT);
        long expiredJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.EXPIRED);
        long rejectedJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.REJECTED);

        long totalApplications = applicationRepository.countByJobCompanyId(companyId);
        long newApplicationsThisWeek = applicationRepository.countByJobCompanyIdAndAppliedAtAfter(companyId, weekAgo);
        long pendingApplications = applicationRepository.countByJobCompanyIdAndStatus(companyId, ApplicationStatus.PENDING);
        long reviewingApplications = applicationRepository.countByJobCompanyIdAndStatus(companyId, ApplicationStatus.REVIEWING);
        long interviewApplications = applicationRepository.countByJobCompanyIdAndStatus(companyId, ApplicationStatus.INTERVIEW);
        long hiredApplications = applicationRepository.countByJobCompanyIdAndStatus(companyId, ApplicationStatus.HIRED);

        return new EmployerDashboardStatsResponse(
                activeJobs, pendingApprovalJobs, draftJobs, expiredJobs, rejectedJobs,
                totalApplications, newApplicationsThisWeek,
                pendingApplications, reviewingApplications, interviewApplications, hiredApplications);
    }
}
