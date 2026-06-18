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
import com.yoedu.job_board_platform.services.NotificationService;
import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.CompanyApprovalRequest;
import com.yoedu.job_board_platform.dtos.admin.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.admin.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.mappers.AdminMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.services.AdminService;

import com.yoedu.job_board_platform.specifications.CompanySpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CompanyRepository companyRepository;
    private final CompanyEmployerDetailRepository employerDetailRepository;
    private final AdminMapper adminMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PendingCompanyResponse> getPendingCompanies(
            String keyword,
            Boolean hasTaxCode,
            Boolean hasContact,
            Pageable pageable
    ) {
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
                                (first, ignored) -> first
                        ));

        return companies.map(company ->
                adminMapper.toPendingCompanyResponseSafe(
                        company,
                        detailsByCompanyId.get(company.getId())
                )
        );
    }

    @Override
    @Transactional
    public void approveCompany(UUID companyId, CompanyApprovalRequest request) {
        Company company = findCompany(companyId);

        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        company.setRejectionReason(null);
        company.setApprovedAt(OffsetDateTime.now());

        Company savedCompany = companyRepository.save(company);

        notificationService.notifyCompanyStatusChange(savedCompany.getId(), "CompanyApproved", "Công ty của bạn đã được phê duyệt và hiển thị trên nền tảng.");
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

        notificationService.notifyCompanyStatusChange(savedCompany.getId(), "CompanyRejected", "Công ty của bạn đã bị từ chối.");
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

        notificationService.notifyCompanyStatusChange(savedCompany.getId(), "CompanySuspended", "Công ty của bạn đã bị tạm ngưng hoạt động.");
    }

    private Company findCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay cong ty"));
    }
}