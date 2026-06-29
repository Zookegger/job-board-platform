package com.yoedu.job_board_platform.services.impl;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.application.*;
import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.events.ApplicationStatusChangeEvent;
import com.yoedu.job_board_platform.mappers.ApplicationMapper;
import com.yoedu.job_board_platform.mappers.ApplicationStatusLogMapper;
import com.yoedu.job_board_platform.models.*;
import com.yoedu.job_board_platform.repositories.*;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusLogRepository applicationStatusLogRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final SkillRepository skillRepository;
    private final SecurityUtil securityUtil;
    private final ApplicationMapper applicationMapper;
    private final ApplicationStatusLogMapper applicationStatusLogMapper;
    private final ApplicationEventPublisher eventPublisher;

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

        Optional<Application> existingWithdrawnApp = applicationRepository.findByCandidateIdAndJobId(profile.getId(), job.getId());
        
        Application application;
        if (existingWithdrawnApp.isPresent()) {
            application = existingWithdrawnApp.get();
            applicationMapper.updateEntity(request, application);
        } else {
            application = applicationMapper.toEntity(request);
            application.setCandidate(profile);
            application.setJob(job);
        }

        application.setStatus(ApplicationStatus.PENDING);
        application.setResumeUrl(resume.getFilePath());
        application.setAppliedAt(OffsetDateTime.now());

        Application saved = applicationRepository.save(application);

        applicationStatusLogRepository.save(
                applicationStatusLogMapper.createLog(saved, ApplicationStatus.PENDING, user, "Đơn ứng tuyển đã được gửi"));

        eventPublisher.publishEvent(new ApplicationStatusChangeEvent(saved, ApplicationStatus.PENDING));

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

        List<Application> content = applications.getContent();
        if (content.isEmpty()) {
            return applications.map(applicationMapper::toEmployerListResponse);
        }

        List<UUID> candidateIds = content.stream()
                .map(app -> app.getCandidate().getId())
                .distinct()
                .toList();

        List<CandidateSkill> allSkills = candidateSkillRepository.findAllByIdCandidateIdIn(candidateIds);
        Map<UUID, List<CandidateSkill>> skillsByCandidate = allSkills.stream()
                .collect(Collectors.groupingBy(cs -> cs.getId().getCandidateId()));

        Map<Integer, String> skillNameMap = skillRepository.findAllById(
                allSkills.stream().map(cs -> cs.getId().getSkillId()).toList()).stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getName));

        return applications.map(app -> {
            List<CandidateSkillResponse> skills = skillsByCandidate
                    .getOrDefault(app.getCandidate().getId(), List.of())
                    .stream()
                    .map(cs -> new CandidateSkillResponse(
                            cs.getId().getSkillId(),
                            skillNameMap.getOrDefault(cs.getId().getSkillId(), ""),
                            cs.getProficientLevel()))
                    .toList();
            return applicationMapper.toEmployerListResponse(app, skills);
        });
    }

    @Override
    @Transactional
    public void updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus, String reason) {
        User employer = securityUtil.getCurrentUser();
        Profile profile = employer.getProfile();

        if (profile == null || profile.getEmployerDetail() == null || !employer.getRole().equals(UserRole.EMPLOYER)) {
            throw new ForbiddenException("Bạn không có quyền truy cập tài nguyên");
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
        applicationStatusLogRepository.save(applicationStatusLogMapper.createLog(application, newStatus, employer, note));

        eventPublisher.publishEvent(new ApplicationStatusChangeEvent(application, newStatus));
    }
}
