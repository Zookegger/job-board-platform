package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.EmployerApplicationApi;
import com.yoedu.job_board_platform.dtos.application.EmployerApplicationListResponse;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/employer/applications")
@PreAuthorize(AuthorizationConstants.EMPLOYER)
@RequiredArgsConstructor
public class EmployerApplicationController implements EmployerApplicationApi {

    private final ApplicationService applicationService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<Page<EmployerApplicationListResponse>> getApplications(
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page) {

        Profile profile = securityUtil.getCurrentUser().getProfile();
        CompanyEmployerDetail employerDetail = profile != null ? profile.getEmployerDetail() : null;
        Company company = employerDetail != null ? employerDetail.getCompany() : null;
        if (company == null) {
            return ResponseEntity.ok(Page.empty());
        }

        ApplicationStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ApplicationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // trả về rỗng nếu status không hợp lệ
                return ResponseEntity.ok(Page.empty());
            }
        }

        Page<EmployerApplicationListResponse> result = applicationService.getEmployerApplications(
                company.getId(), jobId, statusEnum,
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "appliedAt")));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationDetail(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {

        ApplicationStatus newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        applicationService.updateApplicationStatus(id, newStatus, reason);
        return ResponseEntity.ok().build();
    }
}
