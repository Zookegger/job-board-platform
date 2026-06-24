package com.yoedu.job_board_platform.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.Resume;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ResumeRepository;
import com.yoedu.job_board_platform.services.impl.ApplicationServiceImpl;
import com.yoedu.job_board_platform.utils.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private SecurityUtil securityUtil;

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

    // ─── submitApplication ──────────────────────────────────────────────────

    @Test
    void TC_01_submitApplication_success_withCoverLetter() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        Resume resume = buildResume(profile);
        ApplicationRequest request = new ApplicationRequest(job.getId(), "Thư xin việc");

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(), ApplicationStatus.WITHDRAWN)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(profile.getId())).thenReturn(Optional.of(resume));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        ApplicationResponse result = applicationService.submitApplication(request);

        assertThat(result).isNotNull();
        assertThat(result.jobId()).isEqualTo(job.getId());
        assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(result.resumeUrl()).isEqualTo("/uploads/resumes/cv.pdf");
        assertThat(result.coverLetter()).isEqualTo("Thư xin việc");
    }

    @Test
    void TC_02_submitApplication_success_withoutCoverLetter() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        Job job = buildActiveJob();
        Resume resume = buildResume(profile);
        ApplicationRequest request = new ApplicationRequest(job.getId(), null);

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), job.getId(), ApplicationStatus.WITHDRAWN)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(profile.getId())).thenReturn(Optional.of(resume));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        ApplicationResponse result = applicationService.submitApplication(request);

        assertThat(result.coverLetter()).isNull();
        assertThat(result.resumeUrl()).isEqualTo("/uploads/resumes/cv.pdf");
    }

    @Test
    void TC_03_submitApplication_throwsNotFound_whenJobMissing() {
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
    void TC_04_submitApplication_throwsBadRequest_whenJobNotActive() {
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
    void TC_05_submitApplication_throwsConflict_whenAlreadyApplied() {
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
    void TC_06_submitApplication_throwsBadRequest_whenNoCv() {
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
    void TC_07_submitApplication_throwsNotFound_whenNoProfile() {
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
    void TC_08_checkApplied_returnsTrue_whenAlreadyApplied() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID jobId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), jobId, ApplicationStatus.WITHDRAWN)).thenReturn(true);

        boolean result = applicationService.checkApplied(jobId);

        assertThat(result).isTrue();
    }

    @Test
    void TC_09_checkApplied_returnsFalse_whenNotApplied() {
        Profile profile = buildProfile();
        User user = buildUser(profile);
        UUID jobId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);
        when(applicationRepository.existsByCandidateIdAndJobIdAndStatusNot(profile.getId(), jobId, ApplicationStatus.WITHDRAWN)).thenReturn(false);

        boolean result = applicationService.checkApplied(jobId);

        assertThat(result).isFalse();
    }

    @Test
    void TC_10_checkApplied_returnsFalse_whenNoProfile() {
        User user = buildUser(null);
        UUID jobId = UUID.randomUUID();

        when(securityUtil.getCurrentUser()).thenReturn(user);

        boolean result = applicationService.checkApplied(jobId);

        assertThat(result).isFalse();
        verify(applicationRepository, never()).existsByCandidateIdAndJobIdAndStatusNot(any(), any(), any());
    }

    // ─── withdrawApplication ───────────────────────────────────────────────

    @Test
    void TC_11_withdrawApplication_success_whenPending() {
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
    void TC_12_withdrawApplication_throwsBadRequest_whenNotPending() {
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
    void TC_13_withdrawApplication_throwsForbidden_whenNotOwner() {
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
}
