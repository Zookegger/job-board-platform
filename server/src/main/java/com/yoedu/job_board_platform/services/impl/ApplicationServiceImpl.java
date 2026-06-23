package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.Resume;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ResumeRepository;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final SecurityUtil securityUtil;

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

        if (applicationRepository.existsByCandidateIdAndJobId(profile.getId(), job.getId())) {
            throw new BadRequestException("Bạn đã nộp đơn ứng tuyển cho tin này rồi");
        }

        Resume resume = resumeRepository.findByCandidateDetailProfileId(profile.getId())
                .orElseThrow(() -> new BadRequestException("Bạn chưa upload CV. Vui lòng upload CV trước khi ứng tuyển."));

        Application application = Application.builder()
                .candidate(profile)
                .job(job)
                .coverLetter(request.coverLetter())
                .resumeUrl(resume.getFilePath())
                .appliedAt(OffsetDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);

        return new ApplicationResponse(
                saved.getId(),
                job.getId(),
                job.getTitle(),
                job.getCompany().getCompanyName(),
                saved.getStatus(),
                saved.getCoverLetter(),
                saved.getResumeUrl(),
                saved.getAppliedAt()
        );
    }

    @Override
    public boolean checkApplied(UUID jobId) {
        User user = securityUtil.getCurrentUser();
        Profile profile = user.getProfile();
        if (profile == null) {
            return false;
        }
        return applicationRepository.existsByCandidateIdAndJobId(profile.getId(), jobId);
    }
}
