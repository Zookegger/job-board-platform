package com.yoedu.job_board_platform.services;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.dtos.application.EmployerApplicationListResponse;
import com.yoedu.job_board_platform.mappers.ApplicationMapper;
import com.yoedu.job_board_platform.mappers.ApplicationStatusLogMapper;
import com.yoedu.job_board_platform.models.*;
import com.yoedu.job_board_platform.repositories.*;
import com.yoedu.job_board_platform.services.impl.ApplicationServiceImpl;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusLogRepository applicationStatusLogRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ApplicationStatusLogMapper applicationStatusLogMapper;

    @Mock
    private CandidateSkillRepository candidateSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    // ─── Helpers ────────────────────────────────────────────────────────────

    private User buildUser(Profile profile) {
        User user = new User();
        user.setProfile(profile);
        return user;
    }

    private Profile buildProfile() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        return profile;
    }

    private Job buildActiveJob() {
        Company company = new Company();
        company.setCompanyName("ACME Corp");

        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setTitle("Backend Developer");
        job.setStatus(JobStatus.ACTIVE);
        job.setCompany(company);
        return job;
    }

    private Resume buildResume(Profile profile) {
        Resume resume = new Resume();
        resume.setId(UUID.randomUUID());
        resume.setFilePath("/uploads/resumes/cv.pdf");
        return resume;
    }

    // ─── Employer helpers ─────────────────────────────────────────────────

    private Company buildCompany() {
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setCompanyName("ACME Corp");
        return company;
    }

    private Job buildJobWithCompany(Company company, String title) {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setTitle(title);
        job.setStatus(JobStatus.ACTIVE);
        job.setCompany(company);
        return job;
    }

    private User buildEmployerUser(Company company) {
        Profile profile = buildProfile();
        CompanyEmployerDetail detail = CompanyEmployerDetail.builder()
                .profile(profile)
                .company(company)
                .roleInCompany("HR Manager")
                .build();
        profile.setEmployerDetail(detail);
        User user = buildUser(profile);
        user.setRole(UserRole.EMPLOYER);
        return user;
    }

    private Application buildApplication(Profile candidate, Job job, ApplicationStatus status) {
        return Application.builder()
                .id(UUID.randomUUID())
                .candidate(candidate)
                .job(job)
                .status(status)
                .build();
    }

    // ─── submitApplication ──────────────────────────────────────────────────

    @Test
    void submitApplication_success_withCoverLetter() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        Resume resume = buildResume(profile);
        ApplicationRequest request = new ApplicationRequest(job.getId(), "Thư xin việc");

        Application mappedApp = Application.builder()
                .coverLetter("Thư xin việc")
                .resumeUrl("/uploads/resumes/cv.pdf")
                .status(ApplicationStatus.PENDING)
                .build();

        ApplicationResponse responseDto = new ApplicationResponse(
                null, job.getId(), job.getSlug(), job.getTitle(), job.getCompany().getCompanyName(),
                job.getCompany().getLogoUrl(), job.getLocation(),
                ApplicationStatus.PENDING, "Thư xin việc", "/uploads/resumes/cv.pdf", null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(), ApplicationStatus.WITHDRAWN)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(profile.getId())).thenReturn(Optional.of(resume));
        when(applicationMapper.toEntity(request)).thenReturn(mappedApp);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });
        when(applicationMapper.toDetailResponse(any(Application.class))).thenReturn(responseDto);

        ApplicationResponse result = applicationService.submitApplication(request);

        assertThat(result).isNotNull();
        assertThat(result.jobId()).isEqualTo(job.getId());
        assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(result.resumeUrl()).isEqualTo("/uploads/resumes/cv.pdf");
        assertThat(result.coverLetter()).isEqualTo("Thư xin việc");
    }

    @Test
    void submitApplication_success_withoutCoverLetter() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        Resume resume = buildResume(profile);
        ApplicationRequest request = new ApplicationRequest(job.getId(), null);

        Application mappedApp = Application.builder()
                .coverLetter(null)
                .resumeUrl("/uploads/resumes/cv.pdf")
                .status(ApplicationStatus.PENDING)
                .build();

        ApplicationResponse responseDto = new ApplicationResponse(
                null, job.getId(), job.getSlug(), job.getTitle(), job.getCompany().getCompanyName(),
                job.getCompany().getLogoUrl(), job.getLocation(),
                ApplicationStatus.PENDING, null, "/uploads/resumes/cv.pdf", null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(), ApplicationStatus.WITHDRAWN)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(profile.getId())).thenReturn(Optional.of(resume));
        when(applicationMapper.toEntity(request)).thenReturn(mappedApp);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });
        when(applicationMapper.toDetailResponse(any(Application.class))).thenReturn(responseDto);

        ApplicationResponse result = applicationService.submitApplication(request);

        assertThat(result.coverLetter()).isNull();
        assertThat(result.resumeUrl()).isEqualTo("/uploads/resumes/cv.pdf");
    }

    @Test
    void submitApplication_throwsNotFound_whenJobMissing() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID jobId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("tin tuyển dụng");
    }

    @Test
    void submitApplication_throwsBadRequest_whenJobNotActive() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        job.setStatus(JobStatus.EXPIRED);
        ApplicationRequest request = new ApplicationRequest(job.getId(), null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không còn nhận hồ sơ");
    }

    @Test
    void submitApplication_throwsConflict_whenAlreadyApplied() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        ApplicationRequest request = new ApplicationRequest(job.getId(), null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(), ApplicationStatus.WITHDRAWN)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("đã nộp đơn");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void submitApplication_throwsBadRequest_whenNoCv() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        ApplicationRequest request = new ApplicationRequest(job.getId(), null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(), ApplicationStatus.WITHDRAWN)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(profile.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CV");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void submitApplication_throwsNotFound_whenNoProfile() {
        User user = buildUser(null);
        UUID jobId = UUID.randomUUID();
        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("hồ sơ");

        verify(applicationRepository, never()).save(any());
    }

    // ─── checkApplied ───────────────────────────────────────────────────────

    @Test
    void checkApplied_returnsTrue_whenAlreadyApplied() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID jobId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), jobId, ApplicationStatus.WITHDRAWN)).thenReturn(true);

        boolean result = applicationService.checkApplied(jobId);

        assertThat(result).isTrue();
    }

    @Test
    void checkApplied_returnsFalse_whenNotApplied() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID jobId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), jobId, ApplicationStatus.WITHDRAWN)).thenReturn(false);

        boolean result = applicationService.checkApplied(jobId);

        assertThat(result).isFalse();
    }

    @Test
    void checkApplied_returnsFalse_whenNoProfile() {
        User user = buildUser(null);
        UUID jobId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);

        boolean result = applicationService.checkApplied(jobId);

        assertThat(result).isFalse();
        verify(applicationRepository, never()).existsByCandidateIdAndJobIdAndStatusNot(any(), any(), any());
    }

    // ─── withdrawApplication ───────────────────────────────────────────────

    @Test
    void withdrawApplication_success_whenPending() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID appId = UUID.randomUUID();

        Application application = Application.builder()
                .id(appId)
                .candidate(profile)
                .status(ApplicationStatus.PENDING)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);

        applicationService.withdrawApplication(appId);

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
        verify(applicationRepository).save(application);
    }

    @Test
    void withdrawApplication_throwsBadRequest_whenNotPending() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID appId = UUID.randomUUID();

        Application application = Application.builder()
                .id(appId)
                .candidate(profile)
                .status(ApplicationStatus.REVIEWING)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.withdrawApplication(appId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("chờ duyệt");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void withdrawApplication_throwsForbidden_whenNotOwner() {
        Profile owner = buildProfile();
        Profile other = buildProfile();
        User user = buildUser(other);
        UUID appId = UUID.randomUUID();

        Application application = Application.builder()
                .id(appId)
                .candidate(owner)
                .status(ApplicationStatus.PENDING)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.withdrawApplication(appId))
                .isInstanceOf(ForbiddenException.class);

        verify(applicationRepository, never()).save(any());
    }

    // ─── getTimeline ──────────────────────────────────────────────────────

    @Test
    void getTimeline_success() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID appId = UUID.randomUUID();

        Application application = Application.builder()
                .id(appId)
                .candidate(profile)
                .status(ApplicationStatus.INTERVIEW)
                .build();

        ApplicationStatusLog log = ApplicationStatusLog.builder()
                .id(UUID.randomUUID())
                .application(application)
                .status(ApplicationStatus.PENDING)
                .changedBy(user)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));
        when(applicationStatusLogRepository.findByApplicationIdOrderByChangedAtAsc(appId))
                .thenReturn(List.of(log));
        when(applicationStatusLogMapper.toTimelineResponseList(any())).thenReturn(List.of());

        var result = applicationService.getTimeline(appId);

        assertThat(result).isNotNull();
        verify(applicationStatusLogMapper).toTimelineResponseList(any());
    }

    @Test
    void getTimeline_throwsForbidden_whenNotOwner() {
        Profile owner = buildProfile();
        Profile other = buildProfile();
        User user = buildUser(other);
        UUID appId = UUID.randomUUID();

        Application application = Application.builder()
                .id(appId)
                .candidate(owner)
                .status(ApplicationStatus.PENDING)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.getTimeline(appId))
                .isInstanceOf(ForbiddenException.class);

        verify(applicationStatusLogRepository, never()).findByApplicationIdOrderByChangedAtAsc(any());
    }

    // ─── getEmployerApplications ──────────────────────────────────────────

    @Test
    void getEmployerApplications_success_allFilters() {
        Company company = buildCompany();
        UUID companyId = company.getId();
        UUID jobId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Profile candidate = buildProfile();
        Job job = buildJobWithCompany(company, "Backend Developer");
        Application application = buildApplication(candidate, job, ApplicationStatus.REVIEWING);
        Page<Application> appPage = new PageImpl<>(List.of(application));

        when(applicationRepository.findByJobCompanyIdAndJobIdAndStatus(companyId, jobId, ApplicationStatus.REVIEWING,
                pageable)).thenReturn(appPage);
        when(candidateSkillRepository.findAllByIdCandidateIdIn(List.of(candidate.getId())))
                .thenReturn(List.of());
        when(skillRepository.findAllById(List.of()))
                .thenReturn(List.of());

        EmployerApplicationListResponse response = new EmployerApplicationListResponse(
                application.getId(), candidate.getId(), null, null, null, null,
                jobId, "Backend Developer", ApplicationStatus.REVIEWING,
                null, null, null, List.of());
        when(applicationMapper.toEmployerListResponse(application, List.of()))
                .thenReturn(response);

        Page<EmployerApplicationListResponse> result = applicationService.getEmployerApplications(
                companyId, jobId, ApplicationStatus.REVIEWING, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().jobTitle()).isEqualTo("Backend Developer");
        verify(applicationRepository).findByJobCompanyIdAndJobIdAndStatus(
                companyId, jobId, ApplicationStatus.REVIEWING, pageable);
    }

    @Test
    void getEmployerApplications_success_noFilters() {
        Company company = buildCompany();
        UUID companyId = company.getId();
        Pageable pageable = PageRequest.of(0, 10);

        Profile candidate = buildProfile();
        Job job = buildJobWithCompany(company, "Frontend Developer");
        Application application = buildApplication(candidate, job, ApplicationStatus.PENDING);
        Page<Application> appPage = new PageImpl<>(List.of(application));

        when(applicationRepository.findByJobCompanyId(companyId, pageable))
                .thenReturn(appPage);
        when(candidateSkillRepository.findAllByIdCandidateIdIn(List.of(candidate.getId())))
                .thenReturn(List.of());
        when(skillRepository.findAllById(List.of()))
                .thenReturn(List.of());

        EmployerApplicationListResponse response = new EmployerApplicationListResponse(
                application.getId(), candidate.getId(), null, null, null, null,
                null, "Frontend Developer", ApplicationStatus.PENDING,
                null, null, null, List.of());
        when(applicationMapper.toEmployerListResponse(application, List.of()))
                .thenReturn(response);

        Page<EmployerApplicationListResponse> result = applicationService.getEmployerApplications(
                companyId, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(applicationRepository).findByJobCompanyId(companyId, pageable);
    }

    @Test
    void getEmployerApplications_emptyPage() {
        Company company = buildCompany();
        UUID companyId = company.getId();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Application> emptyPage = Page.empty();
        when(applicationRepository.findByJobCompanyId(companyId, pageable))
                .thenReturn(emptyPage);

        Page<EmployerApplicationListResponse> result = applicationService.getEmployerApplications(
                companyId, null, null, pageable);

        assertThat(result).isEmpty();
        verify(applicationMapper, never()).toEmployerListResponse(any(Application.class), any());
        verify(candidateSkillRepository, never()).findAllByIdCandidateIdIn(any());
    }

    // ─── updateApplicationStatus ──────────────────────────────────────────

    @Test
    void updateApplicationStatus_success() {
        Company company = buildCompany();
        User employer = buildEmployerUser(company);
        UUID appId = UUID.randomUUID();

        Profile candidate = buildProfile();
        Job job = buildJobWithCompany(company, "Backend Developer");
        Application application = buildApplication(candidate, job, ApplicationStatus.PENDING);

        ApplicationStatusLog log = ApplicationStatusLog.builder().build();

        when(securityUtil.getCurrentUser()).thenReturn(employer);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));
        when(applicationStatusLogMapper.createLog(application, ApplicationStatus.REVIEWING, employer, "Đang xem xét"))
                .thenReturn(log);

        applicationService.updateApplicationStatus(appId, ApplicationStatus.REVIEWING, "Đang xem xét");

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REVIEWING);
        verify(applicationRepository).save(application);
        verify(applicationStatusLogRepository).save(log);
    }

    @Test
    void updateApplicationStatus_throwsForbidden_noCompany() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID appId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(appId, ApplicationStatus.REVIEWING, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("không có quyền truy cập");

        verify(applicationRepository, never()).findById(any());
    }

    @Test
    void updateApplicationStatus_throwsForbidden_wrongCompany() {
        Company employerCompany = buildCompany();
        Company otherCompany = buildCompany();
        User employer = buildEmployerUser(employerCompany);
        UUID appId = UUID.randomUUID();

        Profile candidate = buildProfile();
        Job job = buildJobWithCompany(otherCompany, "Backend Developer");
        Application application = buildApplication(candidate, job, ApplicationStatus.PENDING);

        when(securityUtil.getCurrentUser()).thenReturn(employer);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(appId, ApplicationStatus.REVIEWING, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("không có quyền");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void updateApplicationStatus_throwsBadRequest_invalidStatus() {
        Company company = buildCompany();
        User employer = buildEmployerUser(company);
        UUID appId = UUID.randomUUID();

        Profile candidate = buildProfile();
        Job job = buildJobWithCompany(company, "Backend Developer");
        Application application = buildApplication(candidate, job, ApplicationStatus.PENDING);

        when(securityUtil.getCurrentUser()).thenReturn(employer);
        when(applicationRepository.findById(appId)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(appId, ApplicationStatus.PENDING, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Trạng thái không hợp lệ");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void updateApplicationStatus_throwsNotFound() {
        Company company = buildCompany();
        User employer = buildEmployerUser(company);
        UUID appId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(employer);
        when(applicationRepository.findById(appId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(appId, ApplicationStatus.REVIEWING, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("đơn ứng tuyển");
    }
}
