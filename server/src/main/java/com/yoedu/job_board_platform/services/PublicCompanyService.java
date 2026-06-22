package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.dtos.publics.PublicCompanyJobResponse;
import com.yoedu.job_board_platform.dtos.publics.PublicCompanyResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicCompanyService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    public PublicCompanyResponse getCompanyPublicDetail(UUID companyId) {
        Company company = companyRepository
                .findByIdAndStatusAndIsApprovedTrue(companyId, CompanyStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công ty hoặc công ty chưa được duyệt"));

        long totalOpenJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.ACTIVE);

        return PublicCompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .slug(company.getSlug())
                .logoUrl(company.getLogoUrl())
                .description(company.getDescription())
                .website(company.getWebsite())
                .email(company.getEmail())
                .phone(company.getPhone())
                .address(company.getAddress())
                .taxCode(company.getTaxCode())
                .createdAt(company.getCreatedAt())
                .totalOpenJobs(totalOpenJobs)
                .build();
    }

    public Page<PublicCompanyJobResponse> getPublicJobsByCompany(UUID companyId, Pageable pageable) {
        return jobRepository
                .findByCompanyIdAndStatus(companyId, JobStatus.ACTIVE, pageable)
                .map(this::mapToPublicJobResponse);
    }

    private PublicCompanyJobResponse mapToPublicJobResponse(Job job) {
        return PublicCompanyJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .location(job.getLocation())
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .createdAt(job.getCreatedAt())
                .build();
    }
}