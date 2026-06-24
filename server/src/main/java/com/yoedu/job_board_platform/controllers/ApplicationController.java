package com.yoedu.job_board_platform.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.ApplicationApi;
import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/applications")
@PreAuthorize(AuthorizationConstants.CANDIDATE)
@RequiredArgsConstructor
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<?> submitApplication(@RequestBody @Valid ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.submitApplication(request));
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationListResponse>> getMyApplications(
            @RequestParam(required = false) ApplicationStatus status, Pageable pageable) {
        UUID candidateId = securityUtil.getCurrentUserId();
        Page<ApplicationListResponse> result = applicationService.getCandidateApplications(
                candidateId, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationDetail(@PathVariable UUID id) {
        return ResponseEntity.ok("Chi tiết đơn");
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> getApplicationTimeline(@PathVariable UUID id) {
        return ResponseEntity.ok("Timeline đơn ứng tuyển");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawApplication(@PathVariable UUID id) {
        applicationService.withdrawApplication(id);
        return ResponseEntity.ok("Rút hồ sơ thành công");
    }

    @GetMapping("/cv/application/{id}")
    @PreAuthorize(AuthorizationConstants.CANDIDATE_OR_EMPLOYER)
    public ResponseEntity<?> getApplicationCV(@PathVariable UUID id) {
        return ResponseEntity.ok("CV của đơn");
    }

    @GetMapping("/by-job/{jobId}")
    public ResponseEntity<?> getApplicationByJob(@PathVariable UUID jobId) {
        boolean applied = applicationService.checkApplied(jobId);
        UUID applicationId = applicationService.getApplicationIdByJob(jobId);
        return ResponseEntity.ok(Map.of(
                "applied", applied,
                "applicationId", applicationId));
    }
}
