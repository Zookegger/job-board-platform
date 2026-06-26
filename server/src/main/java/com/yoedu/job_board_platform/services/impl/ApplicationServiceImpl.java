package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationCheckResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationTimelineResponse;
import com.yoedu.job_board_platform.dtos.application.EmployerApplicationListResponse;
import com.yoedu.job_board_platform.mappers.ApplicationMapper;
import com.yoedu.job_board_platform.mappers.ApplicationStatusLogMapper;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.ApplicationStatusLog;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.Resume;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.ApplicationStatusLogRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ResumeRepository;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusLogRepository applicationStatusLogRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final SecurityUtil securityUtil;
    private final ApplicationMapper applicationMapper;
    private final ApplicationStatusLogMapper applicationStatusLogMapper;

    @Override
    @Transactional
    public ApplicationResponse submitApplication(ApplicationRequest request) {
        User user = securityUtil.getCurrentUser();
        Profile profile = user.getProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ ứng viên");
        }

        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new BadRequestException("Tin tuyển dụng không còn nhận hồ sơ");
        }

        if (applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(),
                ApplicationStatus.WITHDRAWN)) {
            throw new ConflictException("Bạn đã nộp đơn ứng tuyển cho tin này rồi");
        }

        Resume resume = resumeRepository.findByCandidateDetailProfileId(profile.getId())
                .orElseThrow(
                        () -> new BadRequestException("Bạn chưa upload CV. Vui lòng upload CV trước khi ứng tuyển."));

        Application application = applicationMapper.toEntity(request);
        application.setCandidate(profile);
        application.setJob(job);
        application.setStatus(ApplicationStatus.PENDING);
        application.setResumeUrl(resume.getFilePath());
        application.setAppliedAt(OffsetDateTime.now());

        Application saved = applicationRepository.save(application);

        ApplicationStatusLog initialLog = ApplicationStatusLog.builder()
                .application(saved)
                .status(ApplicationStatus.PENDING)
                .changedBy(user)
                .note("Đơn ứng tuyển đã được gửi")
                .build();
        applicationStatusLogRepository.save(initialLog);

        return applicationMapper.toDetailResponse(saved);
    }

    @Override
    public boolean checkApplied(UUID jobId) {
        User user = securityUtil.getCurrentUser();
        Profile profile = user.getProfile();
        if (profile == null) {
            return false;
        }
        return applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), jobId,
                ApplicationStatus.WITHDRAWN);
    }

    @Override
    public UUID getApplicationIdByJob(UUID jobId) {
        User user = securityUtil.getCurrentUser();
        Profile profile = user.getProfile();
        if (profile == null)
            return null;
        return applicationRepository.findByCandidateIdAndJobId(profile.getId(), jobId)
                .map(Application::getId)
                .orElse(null);
    }

    @Override
    public ApplicationCheckResponse checkApplicationByJob(UUID jobId) {
        boolean applied = checkApplied(jobId);
        UUID applicationId = getApplicationIdByJob(jobId);
        return new ApplicationCheckResponse(applied, applicationId);
    }

    @Override
    @Transactional
    public void withdrawApplication(UUID id) {
        User user = securityUtil.getCurrentUser();
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ ứng viên");
        }

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn ứng tuyển"));

        if (!application.getCandidate().getId().equals(profile.getId())) {
            throw new ForbiddenException("Bạn không có quyền rút đơn này");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể rút đơn khi đang ở trạng thái chờ duyệt");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationListResponse> getCandidateApplications(
            UUID candidateId, ApplicationStatus status, Pageable pageable) {
        securityUtil.isAuthorized(candidateId, List.of(UserRole.CANDIDATE));
        User user = securityUtil.getCurrentUser();

        Page<Application> applications;
        if (status != null) {
            applications = applicationRepository.findByCandidateIdAndStatus(
                    user.getProfile().getId(), status, pageable);
        } else {
            applications = applicationRepository.findByCandidateId(user.getProfile().getId(), pageable);
        }

        List<ApplicationListResponse> mapped = applications.getContent().stream()
                .map(applicationMapper::toListResponse)
                .toList();
        return new PageImpl<>(mapped, pageable, applications.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationDetail(UUID id) {
        Profile profile = securityUtil.getCurrentUser().getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ ứng viên");
        }

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn ứng tuyển"));

        if (!application.getCandidate().getId().equals(profile.getId())) {
            throw new ForbiddenException("Bạn không có quyền xem đơn này");
        }

        return applicationMapper.toDetailResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationTimelineResponse> getTimeline(UUID applicationId) {
        User user = securityUtil.getCurrentUser();
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ ứng viên");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn ứng tuyển"));

        if (!application.getCandidate().getId().equals(profile.getId())) {
            throw new ForbiddenException("Bạn không có quyền xem timeline của đơn này");
        }

        List<ApplicationStatusLog> logs = applicationStatusLogRepository
                .findByApplicationIdOrderByChangedAtAsc(applicationId);

        return applicationStatusLogMapper.toTimelineResponseList(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployerApplicationListResponse> getEmployerApplications(
            UUID companyId, UUID jobId, ApplicationStatus status, Pageable pageable) {

        Page<Application> applications;
        if (jobId != null && status != null) {
            applications = applicationRepository.findByJobCompanyIdAndJobIdAndStatus(companyId, jobId, status, pageable);
        } else if (jobId != null) {
            applications = applicationRepository.findByJobCompanyIdAndJobId(companyId, jobId, pageable);
        } else if (status != null) {
            applications = applicationRepository.findByJobCompanyIdAndStatus(companyId, status, pageable);
        } else {
            applications = applicationRepository.findByJobCompanyId(companyId, pageable);
        }

        return applications.map(applicationMapper::toEmployerListResponse);
    }

    @Override
    @Transactional
    public void updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus, String reason) {
        User employer = securityUtil.getCurrentUser();
        Profile profile = employer.getProfile();

        if (profile == null || profile.getEmployerDetail() == null) {
            throw new ForbiddenException("Bạn chưa có thông tin công ty");
        }

        Company company = profile.getEmployerDetail().getCompany();

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn ứng tuyển"));

        // Kiểm tra quyền sở hữu
        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("Bạn không có quyền cập nhật đơn ứng tuyển này");
        }

        // Validate trạng thái đích (employer chỉ được dùng 4 trạng thái này)
        if (newStatus == ApplicationStatus.PENDING || newStatus == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + newStatus);
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);

        // Ghi lịch sử thay đổi trạng thái
        String note = (reason != null && !reason.isBlank()) ? reason : null;
        ApplicationStatusLog log = ApplicationStatusLog.builder()
                .application(application)
                .status(newStatus)
                .changedBy(employer)
                .note(note)
                .build();
        applicationStatusLogRepository.save(log);
    }
}
