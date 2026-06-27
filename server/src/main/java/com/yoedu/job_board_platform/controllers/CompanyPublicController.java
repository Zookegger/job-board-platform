package com.yoedu.job_board_platform.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.dtos.company.PublicCompanyListResponse;
import com.yoedu.job_board_platform.dtos.company.PublicCompanyResponse;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.mappers.CompanyMapper;
import com.yoedu.job_board_platform.mappers.JobCategoryMapper;
import com.yoedu.job_board_platform.mappers.JobMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.services.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/companies")
@RequiredArgsConstructor
public class CompanyPublicController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;
    private final JobMapper jobMapper;
    private final JobCategoryMapper jobCategoryMapper;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    @Operation(summary = "Danh sách công ty (công khai)", description = "Lấy danh sách công ty đã được duyệt, phân trang, có tìm kiếm theo tên và ngành nghề.")
    @GetMapping("/public/search")
    public ResponseEntity<Page<PublicCompanyListResponse>> searchPublicCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Set<Integer> categoryId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        String filterStatus = (status != null) ? status : "APPROVED";
        Page<Company> companies = companyService.listCompaniesPage(keyword, filterStatus, categoryId, pageable);

        List<UUID> companyIds = companies.getContent().stream().map(Company::getId).toList();
        if (companyIds.isEmpty()) {
            return ResponseEntity.ok(companies.map(c -> companyMapper.toPublicListResponse(c, 0L, List.of())));
        }

        Map<UUID, List<JobCategoryResponse>> categoriesByCompanyId = jobRepository
                .findDistinctCategoriesByCompanyIdsAndStatus(companyIds, JobStatus.ACTIVE)
                .stream()
                .collect(Collectors.groupingBy(
                        row -> (UUID) ((Object[]) row)[0],
                        Collectors.mapping(row -> jobCategoryMapper.toResponse((JobCategory) ((Object[]) row)[1]),
                                Collectors.toList())));

        Map<UUID, Long> jobCountByCompanyId = companyRepository
                .countByCompanyIdsAndStatus(companyIds, JobStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) ((Object[]) row)[0],
                        row -> (Long) ((Object[]) row)[1]));

        return ResponseEntity.ok(companies.map(company -> {
            UUID id = company.getId();
            List<JobCategoryResponse> cats = categoriesByCompanyId.getOrDefault(id, Collections.emptyList());
            long count = jobCountByCompanyId.getOrDefault(id, 0L);
            return companyMapper.toPublicListResponse(company, count, cats);
        }));
    }

    @Operation(summary = "Chi tiết công ty (công khai)", description = "Lấy thông tin chi tiết của công ty theo slug.")
    @GetMapping("/public/{slug}")
    public ResponseEntity<PublicCompanyResponse> getPublicCompanyDetail(@PathVariable String slug) {
        return ResponseEntity.ok(companyService.getPublicCompanyDetail(slug));
    }

    @Operation(summary = "Việc làm của công ty (công khai)", description = "Lấy danh sách việc làm đang tuyển của công ty theo slug.")
    @GetMapping("/public/{slug}/jobs")
    public ResponseEntity<Page<JobResponse>> getPublicCompanyJobs(
            @PathVariable String slug, Pageable pageable) {
        return ResponseEntity.ok(companyService.getPublicJobsByCompany(slug, pageable).map(jobMapper::toResponse));
    }
}