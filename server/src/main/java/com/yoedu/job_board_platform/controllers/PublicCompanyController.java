package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.dtos.publics.PublicCompanyJobResponse;
import com.yoedu.job_board_platform.dtos.publics.PublicCompanyResponse;
import com.yoedu.job_board_platform.services.PublicCompanyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/company-pages")
public class PublicCompanyController {

    private final PublicCompanyService publicCompanyService;

    @GetMapping("/{companyId}")
    public PublicCompanyResponse getCompanyPublicDetail(@PathVariable UUID companyId) {
        return publicCompanyService.getCompanyPublicDetail(companyId);
    }

    @GetMapping("/{companyId}/jobs")
    public Page<PublicCompanyJobResponse> getPublicJobsByCompany(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return publicCompanyService.getPublicJobsByCompany(companyId, pageable);
    }
}