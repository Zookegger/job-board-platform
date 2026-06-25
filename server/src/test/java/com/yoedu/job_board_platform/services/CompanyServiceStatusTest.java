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

import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.mappers.CompanyMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyApprovalLogRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.impl.CompanyServiceImpl;

@ExtendWith(MockitoExtension.class)
class CompanyServiceStatusTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyApprovalLogRepository companyApprovalLogRepository;

    @InjectMocks
    private CompanyServiceImpl service;

    // ── helpers ──────────────────────────────────────────────────────────────

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

    private User buildEmployer(Company company) {
        CompanyEmployerDetail detail = CompanyEmployerDetail.builder()
                .profileId(UUID.randomUUID())
                .company(company)
                .build();
        Profile profile = Profile.builder()
                .fullName("Test Employer")
                .phone("0900000001")
                .employerDetail(detail)
                .build();
        return User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.EMPLOYER)
                .profile(profile)
                .build();
    }

    // ── getStatusByEmployerId ────────────────────────────────────────────────

    @Test
    void getStatusByEmployerId_WhenFound_ReturnsPendingStatus() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.PENDING);
        User employer = buildEmployer(company);
        CompanyStatusResponse expected = new CompanyStatusResponse(
                company.getId(), "Yoedu Corp", "0123456789",
                CompanyStatus.PENDING, company.getCreatedAt(), null, null);

        when(userRepository.findById(employerId)).thenReturn(Optional.of(employer));
        when(companyMapper.toStatusResponse(company)).thenReturn(expected);

        CompanyStatusResponse response = service.getStatusByEmployerId(employerId);

        assertThat(response.approvalStatus()).isEqualTo(CompanyStatus.PENDING);
        assertThat(response.name()).isEqualTo("Yoedu Corp");
        assertThat(response.reviewedAt()).isNull();
    }

    @Test
    void getStatusByEmployerId_WhenFound_ReturnsApprovedStatus() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.APPROVED);
        User employer = buildEmployer(company);
        CompanyStatusResponse expected = new CompanyStatusResponse(
                company.getId(), "Yoedu Corp", "0123456789",
                CompanyStatus.APPROVED, company.getCreatedAt(), null, company.getApprovedAt());

        when(userRepository.findById(employerId)).thenReturn(Optional.of(employer));
        when(companyMapper.toStatusResponse(company)).thenReturn(expected);

        CompanyStatusResponse response = service.getStatusByEmployerId(employerId);

        assertThat(response.approvalStatus()).isEqualTo(CompanyStatus.APPROVED);
        assertThat(response.companyId()).isEqualTo(company.getId());
        assertThat(response.taxCode()).isEqualTo("0123456789");
        assertThat(response.reviewedAt()).isNotNull();
        assertThat(response.reviewNote()).isNull();
    }

    @Test
    void getStatusByEmployerId_WhenFound_ReturnsRejectedStatus() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.REJECTED);
        User employer = buildEmployer(company);
        CompanyStatusResponse expected = new CompanyStatusResponse(
                company.getId(), "Yoedu Corp", "0123456789",
                CompanyStatus.REJECTED, company.getCreatedAt(), "Giay phep khong hop le", company.getApprovedAt());

        when(userRepository.findById(employerId)).thenReturn(Optional.of(employer));
        when(companyMapper.toStatusResponse(company)).thenReturn(expected);

        CompanyStatusResponse response = service.getStatusByEmployerId(employerId);

        assertThat(response.approvalStatus()).isEqualTo(CompanyStatus.REJECTED);
        assertThat(response.reviewNote()).isEqualTo("Giay phep khong hop le");
    }

    @Test
    void getStatusByEmployerId_WhenUserNotFound_ThrowsNotFoundException() {
        UUID employerId = UUID.randomUUID();
        when(userRepository.findById(employerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatusByEmployerId(employerId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getStatusByEmployerId_WhenNoCompany_ThrowsResourceNotFoundException() {
        UUID employerId = UUID.randomUUID();
        Profile profile = Profile.builder()
                .fullName("No Company")
                .phone("0900000099")
                .employerDetail(null)
                .build();
        User employer = User.builder()
                .id(employerId)
                .role(UserRole.EMPLOYER)
                .profile(profile)
                .build();

        when(userRepository.findById(employerId)).thenReturn(Optional.of(employer));

        assertThatThrownBy(() -> service.getStatusByEmployerId(employerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getHistoryByEmployerId ────────────────────────────────────────────────

    @Test
    void getHistoryByEmployerId_ReturnsEmptyList() {
        UUID employerId = UUID.randomUUID();
        Company company = buildCompany(CompanyStatus.APPROVED);
        User employer = buildEmployer(company);

        when(userRepository.findById(employerId)).thenReturn(Optional.of(employer));
        when(companyApprovalLogRepository.findByCompanyIdOrderByCreatedAtDesc(company.getId()))
                .thenReturn(List.of());

        List<ApprovalLogResponse> result = service.getHistoryByEmployerId(employerId);

        assertThat(result).isEmpty();
    }
}
