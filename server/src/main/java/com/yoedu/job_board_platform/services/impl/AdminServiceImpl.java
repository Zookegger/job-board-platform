package com.yoedu.job_board_platform.services.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.util.StringUtils;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.AdminApplicationChartResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminCompanyListResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminDashboardStatsResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminJobListResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminUserListResponse;
import com.yoedu.job_board_platform.dtos.admin.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.admin.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingJobResponse;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.mappers.AdminMapper;
import com.yoedu.job_board_platform.mappers.JobMapper;
import com.yoedu.job_board_platform.mappers.ReportMapper;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyApprovalLog;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.ReportStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.CompanyApprovalLogRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.ReportRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.AdminService;
import com.yoedu.job_board_platform.services.NotificationService;
import com.yoedu.job_board_platform.specifications.CompanySpecification;
import com.yoedu.job_board_platform.specifications.JobSpecification;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CompanyRepository companyRepository;
    private final CompanyApprovalLogRepository companyApprovalLogRepository;
    private final CompanyEmployerDetailRepository employerDetailRepository;
    private final JobRepository jobRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final AdminMapper adminMapper;
    private final JobMapper jobMapper;
    private final ReportMapper reportMapper;
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

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
    @Transactional(readOnly = true)
    public Page<AdminCompanyListResponse> getAllCompanies(String keyword, String status, Pageable pageable) {
        Specification<Company> spec = Specification
                .where(CompanySpecification.hasKeyword(keyword))
                .and(CompanySpecification.hasStatus(status));

        return companyRepository.findAll(spec, pageable)
                .map(adminMapper::toAdminCompanyListResponse);
    }

    @Override
    @Transactional
    public void approveCompany(UUID companyId) {
        Company company = findCompany(companyId);
        CompanyStatus oldStatus = company.getStatus();

        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        company.setRejectionReason(null);
        company.setApprovedAt(OffsetDateTime.now());

        Company savedCompany = companyRepository.save(company);
        saveApprovalLog(savedCompany, oldStatus, CompanyStatus.APPROVED, null);

        notificationService.notifyCompanyStatusChange(
                savedCompany.getId(),
                "CompanyApproved",
                "Công ty của bạn đã được phê duyệt và hiển thị trên nền tảng.");
    }

    @Override
    @Transactional
    public void rejectCompany(UUID companyId, CompanyRejectionRequest request) {
        Company company = findCompany(companyId);
        CompanyStatus oldStatus = company.getStatus();

        company.setStatus(CompanyStatus.REJECTED);
        company.setApproved(false);
        company.setApprovedAt(null);
        company.setRejectionReason(request.reason().trim());

        Company savedCompany = companyRepository.save(company);
        saveApprovalLog(savedCompany, oldStatus, CompanyStatus.REJECTED, request.reason().trim());

        notificationService.notifyCompanyStatusChange(
                savedCompany.getId(),
                "CompanyRejected",
                "Công ty của bạn đã bị từ chối.");
    }

    @Override
    @Transactional
    public void suspendCompany(UUID companyId, CompanySuspensionRequest request) {
        Company company = findCompany(companyId);
        CompanyStatus oldStatus = company.getStatus();

        company.setStatus(CompanyStatus.SUSPENDED);
        company.setApproved(false);
        company.setApprovedAt(null);
        company.setSuspensionReason(request.reason().trim());

        Company savedCompany = companyRepository.save(company);
        saveApprovalLog(savedCompany, oldStatus, CompanyStatus.SUSPENDED, request.reason().trim());

        notificationService.notifyCompanyStatusChange(
                savedCompany.getId(),
                "CompanySuspended",
                "Công ty của bạn đã bị tạm ngưng hoạt động.");
    }

    @Override
    @Transactional
    public void unsuspendCompany(UUID companyId) {
        Company company = findCompany(companyId);

        if (company.getStatus() != CompanyStatus.SUSPENDED) {
            throw new BadRequestException("Công ty không ở trạng thái tạm ngưng");
        }

        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        company.setSuspensionReason(null);

        Company savedCompany = companyRepository.save(company);
        saveApprovalLog(savedCompany, CompanyStatus.SUSPENDED, CompanyStatus.APPROVED, null);

        notificationService.notifyCompanyStatusChange(
                savedCompany.getId(),
                "CompanyUnsuspended",
                "Công ty của bạn đã được mở tạm ngưng và hoạt động trở lại.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminJobListResponse> getAllJobs(String status, Pageable pageable) {
        Specification<Job> spec = Specification.where(JobSpecification.hasStatus(status));

        return jobRepository.findAll(spec, pageable)
                .map(jobMapper::toAdminJobListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PendingJobResponse> getPendingJobs(Pageable pageable) {
        return jobRepository.findByStatus(JobStatus.PENDING_APPROVAL, pageable)
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

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatus(status, pageable)
                    .map(reportMapper::toResponse);
        }

        return reportRepository.findAll(pageable)
                .map(reportMapper::toResponse);
    }

    @Override
    @Transactional
    public void reviewReport(UUID reportId, String reviewNotes) {
        Report report = findReport(reportId);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BadRequestException("Báo cáo không ở trạng thái chờ xử lý");
        }

        User currentUser = securityUtil.getCurrentUser();

        report.setStatus(ReportStatus.REVIEWED);
        report.setReviewedBy(currentUser);
        report.setReviewedAt(OffsetDateTime.now());
        report.setReviewNotes(reviewNotes);

        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void dismissReport(UUID reportId, String reviewNotes) {
        Report report = findReport(reportId);

        if (report.getStatus() == ReportStatus.DISMISSED || report.getStatus() == ReportStatus.RESOLVED) {
            throw new BadRequestException("Báo cáo đã được xử lý, không thể bác bỏ");
        }

        User currentUser = securityUtil.getCurrentUser();

        report.setStatus(ReportStatus.DISMISSED);
        report.setReviewedBy(currentUser);
        report.setReviewedAt(OffsetDateTime.now());
        report.setReviewNotes(reviewNotes);

        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void resolveReport(UUID reportId, String reviewNotes) {
        Report report = findReport(reportId);

        if (report.getStatus() != ReportStatus.REVIEWED) {
            throw new BadRequestException("Báo cáo phải ở trạng thái REVIEWED trước khi giải quyết");
        }

        User currentUser = securityUtil.getCurrentUser();

        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewedBy(currentUser);
        report.setReviewedAt(OffsetDateTime.now());
        report.setReviewNotes(reviewNotes);

        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now().minusDays(7);

        return new AdminDashboardStatsResponse(
                userRepository.count(),
                companyRepository.count(),
                jobRepository.countByStatus(JobStatus.ACTIVE),
                applicationRepository.count(),
                userRepository.countByCreatedAtAfter(sevenDaysAgo),
                jobRepository.countByStatus(JobStatus.PENDING_APPROVAL),
                companyRepository.countByStatus(CompanyStatus.PENDING));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListResponse> getUsers(String role, String status, Pageable pageable) {
        UserRole parsedRole = parseUserRole(role);
        Boolean isActive = parseUserActiveStatus(status);

        Page<User> users;

        if (parsedRole != null && isActive != null) {
            users = userRepository.findByRoleAndIsActive(parsedRole, isActive, pageable);
        } else if (parsedRole != null) {
            users = userRepository.findByRole(parsedRole, pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::toAdminUserListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApplicationChartResponse getApplicationChartStats(int days) {
        int normalizedDays = days == 30 ? 30 : 7;

        OffsetDateTime now = OffsetDateTime.now();
        LocalDate toDate = now.toLocalDate();
        LocalDate fromDate = toDate.minusDays(normalizedDays - 1L);

        OffsetDateTime fromDateTime = fromDate.atStartOfDay().atOffset(now.getOffset());
        OffsetDateTime toDateTime = toDate.plusDays(1).atStartOfDay().atOffset(now.getOffset());

        Map<LocalDate, Long> dailyTotals = new HashMap<>();

        for (Object[] row : applicationRepository.countApplicationsByAppliedDateBetween(fromDateTime, toDateTime)) {
            LocalDate date = toLocalDate(row[0]);
            long total = toLong(row[1]);

            dailyTotals.put(date, total);
        }

        List<AdminApplicationChartResponse.DailyApplicationPoint> dailyApplications = new ArrayList<>();

        for (int i = 0; i < normalizedDays; i++) {
            LocalDate date = fromDate.plusDays(i);

            dailyApplications.add(new AdminApplicationChartResponse.DailyApplicationPoint(
                    date,
                    dailyTotals.getOrDefault(date, 0L)));
        }

        List<Object[]> statusRows = applicationRepository.countApplicationsByStatusBetween(fromDateTime, toDateTime);

        long totalApplications = statusRows.stream()
                .mapToLong(row -> toLong(row[1]))
                .sum();

        List<AdminApplicationChartResponse.StatusDistributionPoint> statusDistribution = statusRows.stream()
                .map(row -> {
                    ApplicationStatus applicationStatus = ApplicationStatus.valueOf(row[0].toString());
                    long total = toLong(row[1]);
                    double percentage = totalApplications == 0
                            ? 0
                            : Math.round((total * 10000.0) / totalApplications) / 100.0;

                    return new AdminApplicationChartResponse.StatusDistributionPoint(
                            applicationStatus,
                            total,
                            percentage);
                })
                .toList();

        return new AdminApplicationChartResponse(
                normalizedDays,
                fromDate,
                toDate,
                totalApplications,
                dailyApplications,
                statusDistribution);
    }

    private Company findCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy công ty"));
    }

    private void saveApprovalLog(Company company, CompanyStatus oldStatus, CompanyStatus newStatus, String note) {
        User actor = securityUtil.getCurrentUser();

        CompanyApprovalLog log = CompanyApprovalLog.builder()
                .company(company)
                .actor(actor)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .build();

        companyApprovalLogRepository.save(log);
    }

    private Job findJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));
    }

    private Report findReport(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo"));
    }

    private void notifyEmployer(Job job, String message) {
        if (job.getCompany() == null) {
            return;
        }

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

    private AdminUserListResponse toAdminUserListResponse(User user) {
        String fullName = user.getProfile() != null
                ? user.getProfile().getFullName()
                : null;

        return new AdminUserListResponse(
                user.getId(),
                user.getEmail(),
                fullName,
                user.getRole(),
                user.isActive() ? "ACTIVE" : "INACTIVE",
                user.isActive(),
                user.getCreatedAt());
    }

    private UserRole parseUserRole(String role) {
        if (!StringUtils.hasText(role) || "ALL".equalsIgnoreCase(role)) {
            return null;
        }

        return UserRole.valueOf(role.trim().toUpperCase());
    }

    private Boolean parseUserActiveStatus(String status) {
        if (!StringUtils.hasText(status) || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        if ("ACTIVE".equalsIgnoreCase(status)) {
            return true;
        }

        if ("INACTIVE".equalsIgnoreCase(status)) {
            return false;
        }

        throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ: " + status);
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return LocalDate.parse(value.toString());
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(value.toString());
    }
}