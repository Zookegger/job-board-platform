package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingJobResponse;
import com.yoedu.job_board_platform.mappers.AdminMapper;
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "companyName", "taxCode");

    private final CompanyRepository companyRepository;
    private final CompanyEmployerDetailRepository employerDetailRepository;
    private final JobRepository jobRepository;
    private final NotificationRepository notificationRepository;
    private final AdminMapper adminMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PendingCompanyResponse> getPendingCompanies(
            int page,
            int size,
            String keyword,
            Boolean hasTaxCode,
            Boolean hasContact,
            String sortBy,
            String direction) {
        Page<Company> companies = companyRepository.findAll(
                pendingCompanySpec(keyword, hasTaxCode, hasContact),
                PageRequest.of(safePage(page), safeSize(size), sort(sortBy, direction)));

        List<UUID> companyIds = companies.getContent().stream()
                .map(Company::getId)
                .toList();

        Map<UUID, CompanyEmployerDetail> detailsByCompanyId = companyIds.isEmpty()
                ? Map.of()
                : employerDetailRepository.findByCompany_IdIn(companyIds).stream()
                        .filter(detail -> detail.getCompany() != null)
                        .collect(Collectors.toMap(
                                detail -> detail.getCompany().getId(),
                                Function.identity(),
                                (first, ignored) -> first));

        return companies.map(company -> adminMapper.toPendingCompanyResponseSafe(company, detailsByCompanyId.get(company.getId())));
    }

    @Override
    @Transactional
    public void approveCompany(UUID companyId) {
        Company company = findCompany(companyId);
        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        company.setRejectionReason(null);
        company.setApprovedAt(OffsetDateTime.now());
        companyRepository.save(company);
    }

    @Override
    @Transactional
    public void rejectCompany(UUID companyId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Ly do tu choi la bat buoc");
        }

        Company company = findCompany(companyId);
        company.setStatus(CompanyStatus.REJECTED);
        company.setApproved(false);
        company.setApprovedAt(null);
        company.setRejectionReason(reason.trim());
        companyRepository.save(company);
    }

    private Company findCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cong ty"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PendingJobResponse> getPendingJobs(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return jobRepository.findByStatus(
                JobStatus.PENDING_APPROVAL,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "createdAt")))
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
        if (job.getCompany() == null) return;
        List<CompanyEmployerDetail> details = employerDetailRepository.findByCompany_IdIn(List.of(job.getCompany().getId()));
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

    private Specification<Company> pendingCompanySpec(String keyword, Boolean hasTaxCode, Boolean hasContact) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), CompanyStatus.PENDING));

            String normalizedKeyword = normalizeKeyword(keyword);
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                        like(cb, root, "companyName", pattern),
                        like(cb, root, "email", pattern),
                        like(cb, root, "phone", pattern),
                        like(cb, root, "taxCode", pattern),
                        like(cb, root, "address", pattern),
                        like(cb, root, "website", pattern)));
            }

            if (hasTaxCode != null) {
                predicates.add(hasTaxCode
                        ? hasValue(cb, root, "taxCode")
                        : missingValue(cb, root, "taxCode"));
            }

            if (hasContact != null) {
                Predicate hasEmail = hasValue(cb, root, "email");
                Predicate hasPhone = hasValue(cb, root, "phone");
                predicates.add(hasContact
                        ? cb.or(hasEmail, hasPhone)
                        : cb.and(missingValue(cb, root, "email"), missingValue(cb, root, "phone")));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Predicate like(CriteriaBuilder cb, Root<Company> root, String field, String pattern) {
        return cb.like(cb.lower(root.<String>get(field)), pattern);
    }

    private Predicate hasValue(CriteriaBuilder cb, Root<Company> root, String field) {
        return cb.and(
                cb.isNotNull(root.get(field)),
                cb.notEqual(cb.trim(root.<String>get(field)), ""));
    }

    private Predicate missingValue(CriteriaBuilder cb, Root<Company> root, String field) {
        return cb.or(
                cb.isNull(root.get(field)),
                cb.equal(cb.trim(root.<String>get(field)), ""));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private Sort sort(String sortBy, String direction) {
        String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(sortDirection, field);
    }
}
