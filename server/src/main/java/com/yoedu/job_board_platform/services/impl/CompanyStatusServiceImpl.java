package com.yoedu.job_board_platform.services.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.services.CompanyStatusService;

import lombok.RequiredArgsConstructor;

/**
 * Triển khai CompanyStatusService.
 * Tra cứu công ty qua CompanyEmployerDetail (profileId = userId do @MapsId).
 * Dùng hoàn toàn entity sẵn có, không cần bảng mới.
 */
@Service
@RequiredArgsConstructor
public class CompanyStatusServiceImpl implements CompanyStatusService {

    private final CompanyEmployerDetailRepository companyEmployerDetailRepository;

    @Override
    public CompanyStatusResponse getStatusByEmployerId(UUID employerId) {
        Company company = findCompany(employerId);
        return toStatusResponse(company);
    }

    @Override
    public List<ApprovalLogResponse> getHistoryByEmployerId(UUID employerId) {
        // Lịch sử duyệt chưa được lưu — trả về danh sách rỗng.
        // Có thể mở rộng sau khi thêm bảng approval_logs.
        findCompany(employerId); // validate employer tồn tại
        return List.of();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Company findCompany(UUID employerId) {
        CompanyEmployerDetail detail = companyEmployerDetailRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin công ty cho tài khoản này"));
        return detail.getCompany();
    }

    private CompanyStatusResponse toStatusResponse(Company c) {
        String submittedAt = c.getCreatedAt() != null
                ? c.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;
        String reviewedAt = c.getApprovedAt() != null
                ? c.getApprovedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        return new CompanyStatusResponse(
                c.getId(),
                c.getCompanyName(),
                c.getTaxCode(),
                c.getStatus().name(),
                submittedAt,
                null,          // reviewedBy — chưa lưu trong Company
                c.getRejectionReason(),
                reviewedAt
        );
    }
}
