package com.yoedu.job_board_platform.services;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
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

    private UUID candidateId;
    private UUID jobId;
    private Profile candidateProfile;
    private User candidateUser;
    private Job activeJob;
    private Company company;
    private Resume candidateResume;

    @BeforeEach
    void setUp() {
        candidateId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        candidateProfile = Profile.builder()
                .id(candidateId)
                .fullName("Nguyễn Văn A")
                .phone("0901234567")
                .build();

        candidateUser = User.builder()
                .id(candidateId)
                .email("candidate@test.com")
                .build();
        candidateUser.setProfile(candidateProfile);

        company = Company.builder()
                .id(UUID.randomUUID())
                .companyName("Tech Corp")
                .build();

        activeJob = Job.builder()
                .id(jobId)
                .title("Java Developer")
                .status(JobStatus.ACTIVE)
                .company(company)
                .build();

        candidateResume = Resume.builder()
                .id(UUID.randomUUID())
                .title("CV của tôi")
                .originalFileName("cv.pdf")
                .filePath("uploads/resumes/cv.pdf")
                .fileSize(102400)
                .build();
    }

    // ----------------------------------------------------------------
    // US-28 TC-01: Nộp đơn thành công với job ACTIVE
    // ----------------------------------------------------------------
    @Test
    void submitApplication_success() {
        ApplicationRequest request = new ApplicationRequest(jobId, "Kính gửi nhà tuyển dụng...");

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(activeJob));
        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(candidateId)).thenReturn(Optional.of(candidateResume));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            return Application.builder()
                    .id(UUID.randomUUID())
                    .candidate(a.getCandidate())
                    .job(a.getJob())
                    .coverLetter(a.getCoverLetter())
                    .resumeUrl(a.getResumeUrl())
                    .status(ApplicationStatus.PENDING)
                    .appliedAt(a.getAppliedAt())
                    .build();
        });

        ApplicationResponse response = applicationService.submitApplication(request);

        assertThat(response).isNotNull();
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.jobTitle()).isEqualTo("Java Developer");
        assertThat(response.companyName()).isEqualTo("Tech Corp");
        assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(response.coverLetter()).isEqualTo("Kính gửi nhà tuyển dụng...");
        assertThat(response.resumeUrl()).isEqualTo("uploads/resumes/cv.pdf");
        verify(applicationRepository).save(any(Application.class));
    }

    // ----------------------------------------------------------------
    // US-28 TC-02: Nộp đơn không cần cover letter
    // ----------------------------------------------------------------
    @Test
    void submitApplication_noCoverLetter_success() {
        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(activeJob));
        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(candidateId)).thenReturn(Optional.of(candidateResume));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            return Application.builder()
                    .id(UUID.randomUUID())
                    .candidate(a.getCandidate())
                    .job(a.getJob())
                    .coverLetter(null)
                    .resumeUrl(a.getResumeUrl())
                    .status(ApplicationStatus.PENDING)
                    .appliedAt(a.getAppliedAt())
                    .build();
        });

        ApplicationResponse response = applicationService.submitApplication(request);

        assertThat(response.coverLetter()).isNull();
        assertThat(response.resumeUrl()).isEqualTo("uploads/resumes/cv.pdf");
        verify(applicationRepository).save(any(Application.class));
    }

    // ----------------------------------------------------------------
    // US-28 TC-03: Job không tồn tại → NotFoundException
    // ----------------------------------------------------------------
    @Test
    void submitApplication_jobNotFound_throwsNotFound() {
        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy tin tuyển dụng");

        verify(applicationRepository, never()).save(any());
    }

    // ----------------------------------------------------------------
    // US-28 TC-04: Job không ở trạng thái ACTIVE → BadRequestException
    // ----------------------------------------------------------------
    @Test
    void submitApplication_jobNotActive_throwsBadRequest() {
        Job pendingJob = Job.builder()
                .id(jobId)
                .title("Java Developer")
                .status(JobStatus.PENDING_APPROVAL)
                .company(company)
                .build();

        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(pendingJob));

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không còn nhận hồ sơ");

        verify(applicationRepository, never()).save(any());
    }

    // ----------------------------------------------------------------
    // US-28 TC-05: Job đã EXPIRED → BadRequestException
    // ----------------------------------------------------------------
    @Test
    void submitApplication_jobExpired_throwsBadRequest() {
        Job expiredJob = Job.builder()
                .id(jobId)
                .title("Java Developer")
                .status(JobStatus.EXPIRED)
                .company(company)
                .build();

        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(expiredJob));

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(BadRequestException.class);

        verify(applicationRepository, never()).save(any());
    }

    // ----------------------------------------------------------------
    // US-28 TC-06: Đã nộp đơn rồi (duplicate) → BadRequestException
    // ----------------------------------------------------------------
    @Test
    void submitApplication_duplicateApplication_throwsBadRequest() {
        ApplicationRequest request = new ApplicationRequest(jobId, "Cover letter");

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(activeJob));
        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã nộp đơn");

        verify(applicationRepository, never()).save(any());
    }

    // ----------------------------------------------------------------
    // US-28 TC-07: Ứng viên chưa upload CV → BadRequestException
    // ----------------------------------------------------------------
    @Test
    void submitApplication_noResume_throwsBadRequest() {
        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(candidateUser);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(activeJob));
        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)).thenReturn(false);
        when(resumeRepository.findByCandidateDetailProfileId(candidateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("chưa upload CV");

        verify(applicationRepository, never()).save(any());
    }

    // ----------------------------------------------------------------
    // US-28 TC-08: User không có profile → ResourceNotFoundException
    // ----------------------------------------------------------------
    @Test
    void submitApplication_noProfile_throwsNotFound() {
        User userWithoutProfile = User.builder()
                .id(UUID.randomUUID())
                .email("noprofile@test.com")
                .build();
        // profile = null

        ApplicationRequest request = new ApplicationRequest(jobId, null);

        when(securityUtil.getCurrentUser()).thenReturn(userWithoutProfile);

        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("hồ sơ ứng viên");

        verify(jobRepository, never()).findById(any());
        verify(applicationRepository, never()).save(any());
    }
}
