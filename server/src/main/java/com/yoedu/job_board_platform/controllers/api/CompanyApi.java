package com.yoedu.job_board_platform.controllers.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;

public interface CompanyApi {
    ResponseEntity<CompanyResponse> findCompanyByEmployerId();

    ResponseEntity<CompanyResponse> update(CompanyRequest request);

    ResponseEntity<CompanyResponse> getCompanyByJobPost(UUID jobPostId);

    ResponseEntity<List<CompanyResponse>> listCompanies();
}
