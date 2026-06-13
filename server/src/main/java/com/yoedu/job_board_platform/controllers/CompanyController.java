package com.yoedu.job_board_platform.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.controllers.api.CompanyApi;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;
import com.yoedu.job_board_platform.services.CompanyService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CompanyController implements CompanyApi {
    private final CompanyService companyService;
    private final SecurityUtil securityUtil;

    @Override
    public ResponseEntity<CompanyResponse> findCompanyByEmployerId() {
        UUID employerId = securityUtil.getCurrentUserId();
        CompanyResponse company = companyService.findCompanyByEmployerId(employerId);
        return ResponseEntity.ok(company);
    }

    @Override
    @PutMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CompanyResponse> update(@Valid @RequestBody CompanyRequest request) {
        UUID employerId = securityUtil.getCurrentUserId();
        CompanyResponse updated = companyService.update(employerId, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    @GetMapping
    public ResponseEntity<CompanyResponse> getCompanyByJobPost(UUID jobPostId) {
        return ResponseEntity.ok(companyService.getCompanyByJobPost(jobPostId));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> listCompanies() {
        return ResponseEntity.ok(companyService.listCompanies());
    }
}
