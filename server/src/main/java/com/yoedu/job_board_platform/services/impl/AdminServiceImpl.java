package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.AdminJobListResponse;
import com.yoedu.job_board_platform.dtos.admin.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.admin.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingJobResponse;
import com.yoedu.job_board_platform.mappers.AdminMapper;
import com.yoedu.job_board_platform.mappers.JobMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.services.AdminService;
import com.yoedu.job_board_platform.services.NotificationService;
import com.yoedu.job_board_platform.specifications.CompanySpecification;
import com.yoedu.job_board_platform.specifications.JobSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CompanyRepository companyRepository;
    private final CompanyEmployerDetailRepository employerDetailRepository;
    private final JobRepository jobRepository;
    private final NotificationRepository notificationRepository;
    private final AdminMapper adminMapper;
    private final JobMapper jobMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PendingCompanyResponse> getPendingCompanies(
            String keyword,
            Boolean hasTaxCode,
            Boolean hasContact,
            Pageable pageable) {
        Specification<Company> specification = Specification
                .where(CompanySpecification.isPending())
                .and(CompanySpecification.hasKeyword(keyword))
                .and(CompanySpecification.hasTaxCode(hasTaxCode))
                .and(CompanySpecification.hasContact(hasContact));

        Page<Company> companies = companyRepository.findAll(specification, pageable);

        List<UUID> companyIds = companies.getContent()
                .stream()
                .map(Company::getId)
                .toList();

        Map<UUID, CompanyEmployerDetail> detailsByCompanyId = companyIds.isEmpty()
                ? Map.of()
                : employerDetailRepository.findByCompany_IdIn(companyIds)
                        .stream()
                        .filter(detail -> detail.getCompany() != null)
                        .collect(Collectors.toMap(
                                detail -> detail.getCompany().getId(),
                                Function.identity(),
                                (first, ignored) -> first));

        return companies.map(company -> adminMapper.toPendingCompanyResponseSafe(
                company,
                detailsByCompanyId.get(company.getId())));
    }

    @Override
    @Transactional
    public void approveCompany(UUID companyId) {
        Company company = findCompany(companyId);

        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        company.setRejectionReason(null);
        company.setApprovedAt(OffsetDateTime.now());

        Company savedCompany = companyRepository.save(company);

        notificationService.notifyCompanyStatusChange(savedCompany.getId(), "CompanyApproved",
                "Công ty của bạn đã được phê duyệt và hiển thị trên nền tảng.");
    }

    @Override
    @Transactional
    public void rejectCompany(UUID companyId, CompanyRejectionRequest request) {
        Company company = findCompany(companyId);

        company.setStatus(CompanyStatus.REJECTED);
        company.setApproved(false);
        company.setApprovedAt(null);
        company.setRejectionReason(request.reason().trim());

        Company savedCompany = companyRepository.save(company);

        notificationService.notifyCompanyStatusChange(savedCompany.getId(), "CompanyRejected",
                "Công ty của bạn đã bị từ chối.");
    }

    @Override
    @Transactional
    public void suspendCompany(UUID companyId, CompanySuspensionRequest request) {
        Company company = findCompany(companyId);

        company.setStatus(CompanyStatus.SUSPENDED);
        company.setApproved(false);
        company.setApprovedAt(null);

        company.setSuspensionReason(request.reason().trim());

        Company savedCompany = companyRepository.save(company);

        notificationService.notifyCompanyStatusChange(savedCompany.getId(), "CompanySuspended",
                "Công ty của bạn đã bị tạm ngưng hoạt động.");
    }

    private Company findCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cong ty"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminJobListResponse> getAllJobs(String status, Pageable pageable) {
        Specification<Job> spec = Specification.where(JobSpecification.hasStatus(status));
        return jobRepository.findAll(spec, pageable).map(jobMapper::toAdminJobListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PendingJobResponse> getPendingJobs(Pageable pageable) {
        return jobRepository.findByStatus(
                JobStatus.PENDING_APPROVAL, pageable)
                .map(this::toPendingJobResponse);
    }

    @Override
    @Transactional
    public void approveJob(UUID jobId) {
        Job job = findJob(jobId);
        if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Tin tuyển dụng không ở trạng thái chờ duyệt");
        }
        job.setStatus(JobStatus.ACTIVE);
        job.setRejectionReason(null);
        jobRepository.save(job);
        notifyEmployer(job, "Tin tuyển dụng \"" + job.getTitle() + "\" đã được phê duyệt và hiển thị công khai.");
    }

    @Override
    @Transactional
    public void rejectJob(UUID jobId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Lý do từ chối là bắt buộc");
        }
        Job job = findJob(jobId);
        if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Tin tuyển dụng không ở trạng thái chờ duyệt");
        }
        job.setStatus(JobStatus.REJECTED);
        job.setRejectionReason(reason.trim());
        jobRepository.save(job);
        notifyEmployer(job, "Tin tuyển dụng \"" + job.getTitle() + "\" đã bị từ chối. Lý do: " + reason.trim());
    }

    private void notifyEmployer(Job job, String message) {
        if (job.getCompany() == null)
            return;
        List<CompanyEmployerDetail> details = employerDetailRepository
                .findByCompany_IdIn(List.of(job.getCompany().getId()));
        for (CompanyEmployerDetail detail : details) {
            if (detail.getProfile() != null && detail.getProfile().getUser() != null) {
                notificationRepository.save(Notification.builder()
                        .user(detail.getProfile().getUser())
                        .type(NotificationStatus.JOB_STATUS_CHANGED)
                        .entityId(job.getId())
                        .message(message)
                        .build());
            }
        }
    }

    private Job findJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));
    }

    private PendingJobResponse toPendingJobResponse(Job job) {
        String companyName = job.getCompany() != null ? job.getCompany().getCompanyName() : null;
        String logoUrl = job.getCompany() != null ? job.getCompany().getLogoUrl() : null;
        String categoryName = job.getCategory() != null ? job.getCategory().getName() : null;
        return new PendingJobResponse(
                job.getId(),
                job.getTitle(),
                job.getStatus(),
                job.getDescription(),
                job.getRequirements(),
                job.getBenefits(),
                job.getLocation(),
                job.getLocationTypes(),
                job.getEmploymentType(),
                job.getExperienceLevel(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getNumberOfOpenings(),
                companyName,
                logoUrl,
                categoryName,
                job.getCreatedAt());
    }
}
