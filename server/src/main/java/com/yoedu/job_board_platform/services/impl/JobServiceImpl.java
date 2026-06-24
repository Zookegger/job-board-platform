package com.yoedu.job_board_platform.services.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobRequest;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.mappers.JobMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobSkill;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.services.JobService;
import com.yoedu.job_board_platform.services.JobSkillService;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import com.yoedu.job_board_platform.utils.StringUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private final SecurityUtil securityUtil;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final JobCategoryRepository categoryRepository;
    private final SkillRepository skillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobSkillService jobSkillService;

    /**
     * Tìm kiếm tin tuyển dụng và xác thực quyền sở hữu của công ty.
     *
     * @param jobId     ID của tin tuyển dụng.
     * @param companyId ID của công ty thực hiện yêu cầu.
     * @return Đối tượng {@link Job} hợp lệ.
     * @throws NotFoundException  Nếu không tìm thấy tin tuyển dụng.
     * @throws ForbiddenException Nếu tin tuyển dụng không thuộc về công ty này.
     */
    private Job findJobOwnedByCompany(UUID jobId, UUID companyId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin tuyển dụng"));

        if (!job.getCompany().getId().equals(companyId)) {
            throw new ForbiddenException("Tin tuyển dụng không thuộc về công ty của bạn");
        }

        return job;
    }

    /**
     * Xác thực quyền hạn của nhà tuyển dụng và lấy thông tin công ty tương ứng.
     *
     * @param employerId ID của nhà tuyển dụng.
     * @return Đối tượng {@link Company} thuộc về nhà tuyển dụng.
     * @throws ForbiddenException Nếu sai quyền hạn hoặc thiếu thông tin công ty cấu
     *                            hình trong hồ sơ.
     */
    private Company getAuthorizedEmployerCompany(UUID employerId) {
        securityUtil.isAuthorized(employerId, List.of(UserRole.EMPLOYER));
        User user = securityUtil.getCurrentUser();

        if (user.getProfile() == null ||
                user.getProfile().getEmployerDetail() == null ||
                user.getProfile().getEmployerDetail().getCompany() == null) {
            throw new ForbiddenException("Không tìm thấy thông tin công ty của nhà tuyển dụng");
        }

        return user.getProfile().getEmployerDetail().getCompany();
    }

    @Override
    @Transactional
    public JobResponse createJob(UUID employerId, JobRequest request) {
        Company company = getAuthorizedEmployerCompany(employerId);

        JobCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ngành nghế yêu càu"));

        Job job = jobMapper.toEntity(request);
        job.setCompany(company);
        job.setCategory(category);
        job.setStatus(JobStatus.DRAFT);
        job.setSlug(StringUtils.slugifyUnique(request.title(), slug -> jobRepository.existsBySlug(slug)));

        Job savedJob = jobRepository.save(job);

        if (request.skillIds() != null && !request.skillIds().isEmpty()) {
            List<Skill> skills = skillRepository.findAllById(request.skillIds());
            skills.stream().forEach(skill -> jobSkillRepository.save(new JobSkill(savedJob.getId(), skill.getId())));
        }

        JobResponse response = jobMapper.toResponse(savedJob);
        response = response.withSkills(
                request.skillIds() != null && !request.skillIds().isEmpty()
                        ? jobSkillService.getSkillsByJobId(savedJob.getId())
                        : List.of()
        );

        return response;
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID jobId, UUID employerId, JobRequest request) {
        Company company = getAuthorizedEmployerCompany(employerId);

        Job job = findJobOwnedByCompany(jobId, company.getId());

        // Khi cập nhật bài tuyển dụng, các trạng thái của bài (ACTIVE,
        // PENDING_APPROVAL, REJECTED, EXPIRED) resets về DRAFT
        // Yêu cầu quản trị viên phải review và duyệt lại trước khi được đăng lên
        if (job.getStatus() != JobStatus.DRAFT) {
            job.setStatus(JobStatus.DRAFT);
        }

        if (job.hasCategoriesChanged(request.categoryId())) {
            JobCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy ngành nghế yêu càu"));
            job.setCategory(category);
        }

        jobMapper.updateEntity(request, job);

        Set<Integer> incomingSkillIds = request.skillIds() != null ? request.skillIds() : Set.of();
        jobSkillService.syncJobSkills(job.getId(), incomingSkillIds);

        JobResponse response = jobMapper.toResponse(job);
        response = response.withSkills(
                request.skillIds() != null && !request.skillIds().isEmpty()
                        ? jobSkillService.getSkillsByJobId(job.getId())
                        : List.of()
        );

        return response;
    }

    @Override
    public Page<JobListResponse> getEmployerJobs(UUID employerId, JobStatus status, int page, int size) {
        Company company = getAuthorizedEmployerCompany(employerId);

        int safeSize = size > 0 ? Math.min(size, 100) : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Job> jobs;
        if (status != null) {
            jobs = jobRepository.findByCompanyIdAndStatus(company.getId(), status, pageable);
        } else {
            jobs = jobRepository.findByCompanyId(company.getId(), pageable);
        }

        return jobs.map(jobMapper::toSummary);
    }

    @Override
    public JobResponse getJobDetail(UUID jobId, UUID employerId) {
        Company company = getAuthorizedEmployerCompany(employerId);
        Job job = findJobOwnedByCompany(jobId, company.getId());

        JobResponse response = jobMapper.toResponse(job);
        List<SkillResponse> skills = jobSkillService.getSkillsByJobId(job.getId());

        if (!skills.isEmpty()) {
            response = response.withSkills(skills);
        }
        return response;
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId, UUID employerUUID) {
        Company company = getAuthorizedEmployerCompany(employerUUID);
        Job job = findJobOwnedByCompany(jobId, company.getId());
        jobSkillService.syncJobSkills(jobId, null);
        jobRepository.delete(job);
    }

    @Override
    public void submitForReview(UUID jobId, UUID employerId) {
        Company company = getAuthorizedEmployerCompany(employerId);
        Job job = findJobOwnedByCompany(jobId, company.getId());

        if (job.getStatus() != JobStatus.DRAFT) {
            throw new BadRequestException("Chỉ có thể gửi duyệt tin ở trạng thái DRAFT");
        }

        job.setStatus(JobStatus.PENDING_APPROVAL);
        jobRepository.save(job);
    }

    @Override
    public Page<JobListResponse> getActiveJobs(int page, int size) {
        int safeSize = size > 0 ? Math.min(size, 100) : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jobRepository.findByStatus(JobStatus.ACTIVE, pageable).map(jobMapper::toSummary);
    }

    @Override
    public JobResponse getActiveJobDetail(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tin tuyển dụng"));

        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new NotFoundException("Không tìm thấy tin tuyển dụng");
        }

        JobResponse response = jobMapper.toResponse(job);
        List<SkillResponse> skills = jobSkillService.getSkillsByJobId(job.getId());
        if (!skills.isEmpty()) {
            response = response.withSkills(skills);
        }
        return response;
    }
}
