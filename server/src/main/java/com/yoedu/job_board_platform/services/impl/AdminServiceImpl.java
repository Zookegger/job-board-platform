package com.yoedu.job_board_platform.services.impl;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.*;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.events.CompanyStatusChangeEvent;
import com.yoedu.job_board_platform.events.JobStatusChangeEvent;
import com.yoedu.job_board_platform.mappers.AdminMapper;
import com.yoedu.job_board_platform.mappers.DashboardMapper;
import com.yoedu.job_board_platform.mappers.DashboardMapper.DailyApplicationCount;
import com.yoedu.job_board_platform.mappers.DashboardMapper.StatusApplicationCount;
import com.yoedu.job_board_platform.mappers.JobMapper;
import com.yoedu.job_board_platform.mappers.ReportMapper;
import com.yoedu.job_board_platform.models.*;
import com.yoedu.job_board_platform.repositories.*;
import com.yoedu.job_board_platform.services.AdminService;
import com.yoedu.job_board_platform.specifications.CompanySpecification;
import com.yoedu.job_board_platform.specifications.JobSpecification;
import com.yoedu.job_board_platform.specifications.ReportSpecification;
import com.yoedu.job_board_platform.specifications.UserSpecification;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final CompanyRepository companyRepository;
	private final CompanyApprovalLogRepository companyApprovalLogRepository;
	private final CompanyEmployerDetailRepository employerDetailRepository;
	private final JobRepository jobRepository;
	private final ReportRepository reportRepository;
	private final AdminMapper adminMapper;
	private final DashboardMapper dashboardMapper;
	private final JobMapper jobMapper;
	private final ReportMapper reportMapper;
	private final SecurityUtil securityUtil;
	private final UserRepository userRepository;
	private final ApplicationRepository applicationRepository;
	private final ApplicationEventPublisher eventPublisher;

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
				: employerDetailRepository.findByCompanyIdIn(companyIds)
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

		eventPublisher.publishEvent(new CompanyStatusChangeEvent(savedCompany, CompanyStatus.APPROVED));
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

		eventPublisher.publishEvent(new CompanyStatusChangeEvent(savedCompany, CompanyStatus.REJECTED));
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

		eventPublisher.publishEvent(new CompanyStatusChangeEvent(savedCompany, CompanyStatus.SUSPENDED));
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

		eventPublisher.publishEvent(new CompanyStatusChangeEvent(savedCompany, CompanyStatus.APPROVED));
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
				.map(jobMapper::toPendingJobResponse);
	}

	@Override
	@Transactional
	public void approveJob(UUID jobId) {
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

		if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
			throw new BadRequestException("Tin tuyển dụng không ở trạng thái chờ duyệt");
		}
		job.setStatus(JobStatus.ACTIVE);
		job.setRejectionReason(null);
		jobRepository.save(job);

		eventPublisher.publishEvent(new JobStatusChangeEvent(job, JobStatus.ACTIVE));
	}

	@Override
	@Transactional
	public void rejectJob(UUID jobId, String reason) {
		if (reason == null || reason.isBlank()) {
			throw new BadRequestException("Lý do từ chối là bắt buộc");
		}
		Job job = jobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin tuyển dụng"));

		if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
			throw new BadRequestException("Tin tuyển dụng không ở trạng thái chờ duyệt");
		}
		job.setStatus(JobStatus.REJECTED);
		job.setRejectionReason(reason.trim());
		jobRepository.save(job);

		eventPublisher.publishEvent(new JobStatusChangeEvent(job, JobStatus.REJECTED));
	}

	// ================ Reports ================

	@Override
	@Transactional(readOnly = true)
	public Page<ReportResponse> getReports(ReportStatus status, ReportReason reason, Pageable pageable) {
		Specification<Report> spec = Specification.where(ReportSpecification.hasStatus(status).and(ReportSpecification.hasReason(reason)));

		return reportRepository.findAll(spec, pageable)
				.map(reportMapper::toResponse);
	}

	@Override
	@Transactional
	public void reviewReport(UUID reportId, String reviewNotes) {
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo"));

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
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo"));

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
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy báo cáo"));


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
				companyRepository.countByStatus(CompanyStatus.PENDING)
		);
	}

	@Override
	@Transactional(readOnly = true)
	public AdminApplicationChartResponse getApplicationChartStats(int days) {
		int normalizedDays = (days >= 14) ? 30 : 7;

		OffsetDateTime now = OffsetDateTime.now();
		LocalDate toDate = now.toLocalDate();
		LocalDate fromDate = toDate.minusDays(normalizedDays - 1L);

		OffsetDateTime fromDateTime = fromDate.atStartOfDay().atOffset(now.getOffset());
		OffsetDateTime toDateTime = toDate.plusDays(1).atStartOfDay().atOffset(now.getOffset());

		List<DailyApplicationCount> dailyCounts =
				applicationRepository.countApplicationsByAppliedDateBetween(fromDateTime, toDateTime);

		Map<LocalDate, Long> dailyMap = dailyCounts.stream()
				.collect(Collectors.toMap(DailyApplicationCount::getApplicationDate, DailyApplicationCount::getTotal));

		List<AdminApplicationChartResponse.DailyApplicationPoint> dailyApplications =
				IntStream.range(0, normalizedDays)
						.mapToObj(fromDate::plusDays)
						.map(date -> dashboardMapper.toDailyPoint(date, dailyMap.getOrDefault(date, 0L)))
						.toList();

		List<StatusApplicationCount> statusRows =
				applicationRepository.countApplicationsByStatusBetween(fromDateTime, toDateTime);

		long totalApplications = statusRows.stream()
				.mapToLong(StatusApplicationCount::getTotal)
				.sum();

		List<AdminApplicationChartResponse.StatusDistributionPoint> statusDistribution = statusRows.stream()
				.map(row -> dashboardMapper.toStatusPoint(row, totalApplications))
				.toList();

		return new AdminApplicationChartResponse(
				normalizedDays,
				fromDate,
				toDate,
				totalApplications,
				dailyApplications,
				statusDistribution);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<User> getUsers(String keyword, UserRole role, Boolean isActive, Pageable pageable) {
		Specification<User> specification = Specification.where(UserSpecification.hasKeyword(keyword)).and(UserSpecification.hasRole(role)).and(UserSpecification.isActive(isActive));
		return userRepository.findAll(specification, pageable);
	}
}
