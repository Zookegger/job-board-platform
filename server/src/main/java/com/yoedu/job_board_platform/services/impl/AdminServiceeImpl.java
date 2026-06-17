package com.yoedu.job_board_platform.services.impl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.company.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.services.AdminService;
import com.yoedu.job_board_platform.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai AdminService. Xử lý phê duyệt/từ chối/tạm ngưng công ty.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminServiceeImpl implements AdminService {
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    @Override
    public Page<PendingCompanyResponse> getPendingCompanies(Pageable pageable) {
        log.info("Fetching pending companies, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Company> companies = companyRepository.findByStatus(CompanyStatus.PENDING, pageable);
        return companies.map(this::toResponse);
    }

    @Override
    public void approveCompany(UUID companyId) {
        log.info("Approving company with ID: {}", companyId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId));

        // Cập nhật trạng thái công ty
        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        company.setApprovedAt(OffsetDateTime.now());
        company.setRejectionReason(null);
        company.setReviewReason(null);

        // Lưu vào database
        companyRepository.save(company);
        log.info("Company {} approved successfully", companyId);

        // Gửi thông báo cho nhà tuyển dụng
        notificationService.notifyCompanyStatusChange(companyId, "APPROVED", null);
    }

    @Override
    public void rejectCompany(UUID companyId, String reason) {
        log.info("Rejecting company with ID: {} with reason: {}", companyId, reason);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId));

        // Cập nhật trạng thái công ty
        company.setStatus(CompanyStatus.REJECTED);
        company.setApproved(false);
        company.setRejectionReason(reason);
        company.setApprovedAt(null);
        company.setReviewReason(null);

        // Lưu vào database
        companyRepository.save(company);
        log.info("Company {} rejected successfully", companyId);

        // Gửi thông báo cho nhà tuyển dụng
        notificationService.notifyCompanyStatusChange(companyId, "REJECTED", reason);
    }

    @Override
    public void suspendCompany(UUID companyId, String reason) {
        log.info("Suspending company with ID: {} with reason: {}", companyId, reason);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy công ty với ID: " + companyId));

        // Cập nhật trạng thái công ty
        company.setStatus(CompanyStatus.SUSPENDED);
        company.setApproved(false);
        company.setRejectionReason(reason);
        company.setApprovedAt(null);
        company.setReviewReason(null);

        // Lưu vào database
        companyRepository.save(company);
        log.info("Company {} suspended successfully", companyId);

        // Gửi thông báo cho nhà tuyển dụng
        notificationService.notifyCompanyStatusChange(companyId, "SUSPENDED", reason);
    }

    private PendingCompanyResponse toResponse(Company company) {
        return PendingCompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .email(company.getEmail())
                .phone(company.getPhone())
                .taxCode(company.getTaxCode())
                .address(company.getAddress())
                .description(company.getDescription())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
