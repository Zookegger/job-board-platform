package com.yoedu.job_board_platform.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.services.impl.CompanyStatusServiceImpl;

@ExtendWith(MockitoExtension.class)
class CompanyStatusServiceImplTest {

    @Mock
    private CompanyEmployerDetailRepository companyEmployerDetailRepository;

    @InjectMocks
    private CompanyStatusServiceImpl service;

    private Company buildCompany(CompanyStatus status) {
        Company company = Company.builder()
                .id(UUID.randomUUID())
                .companyName("Yoedu Corp")
                .taxCode("0123456789")
                .status(status)
                .isApproved(status == CompanyStatus.APPROVED)
                .createdAt(OffsetDateTime.now().minusDays(10))
                .build();
        if (status == CompanyStatus.APPROVED || status == CompanyStatus.REJECTED) {
            company.setApprovedAt(OffsetDateTime.now().minusDays(7));
        }
        if (status == CompanyStatus.REJECTED) {
            company.setRejectionReason("Giay phep khong hop le");
        }
        return company;
    }

    private CompanyEmployerDetail wrapInDetail(Company company) {
        return CompanyEmployerDetail.builder()
                .profileId(UUID.randomUUID())
                .company(company)
                .build();
    }

    @Test
    void getStatusByEmployerId_WhenFound_ReturnsPendingStatus() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.PENDING);
        when(companyEmployerDetailRepository.findById(employerId))
                .thenReturn(Optional.of(wrapInDetail(company)));

        CompanyStatusResponse response = service.getStatusByEmployerId(employerId);

        assertThat(response.approvalStatus()).isEqualTo("PENDING");
        assertThat(response.name()).isEqualTo("Yoedu Corp");
        assertThat(response.reviewedAt()).isNull();
    }

    @Test
    void getStatusByEmployerId_WhenFound_ReturnsApprovedStatus() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.APPROVED);
        when(companyEmployerDetailRepository.findById(employerId))
                .thenReturn(Optional.of(wrapInDetail(company)));

        CompanyStatusResponse response = service.getStatusByEmployerId(employerId);

        assertThat(response.approvalStatus()).isEqualTo("APPROVED");
        assertThat(response.companyId()).isEqualTo(company.getId());
        assertThat(response.taxCode()).isEqualTo("0123456789");
        assertThat(response.reviewedAt()).isNotNull();
        assertThat(response.reviewNote()).isNull();
    }

    @Test
    void getStatusByEmployerId_WhenFound_ReturnsRejectedStatus() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.REJECTED);
        when(companyEmployerDetailRepository.findById(employerId))
                .thenReturn(Optional.of(wrapInDetail(company)));

        CompanyStatusResponse response = service.getStatusByEmployerId(employerId);

        assertThat(response.approvalStatus()).isEqualTo("REJECTED");
        assertThat(response.reviewNote()).isEqualTo("Giay phep khong hop le");
    }

    @Test
    void getStatusByEmployerId_WhenNotFound_ThrowsResourceNotFoundException() {
        UUID employerId = UUID.randomUUID();
        when(companyEmployerDetailRepository.findById(employerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatusByEmployerId(employerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getHistoryByEmployerId_ReturnsEmptyList() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.APPROVED);
        when(companyEmployerDetailRepository.findById(employerId))
                .thenReturn(Optional.of(wrapInDetail(company)));

        List<ApprovalLogResponse> result = service.getHistoryByEmployerId(employerId);

        assertThat(result).isEmpty();
    }
}