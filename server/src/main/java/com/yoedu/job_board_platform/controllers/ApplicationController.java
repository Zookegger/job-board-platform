package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.ApplicationApi;
import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.services.ApplicationService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/applications")
@PreAuthorize("hasRole('CANDIDATE')")
@RequiredArgsConstructor
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<?> submitApplication() {
        return ResponseEntity.ok("Nộp hồ sơ thành công");
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationListResponse>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID candidateId = securityUtil.getCurrentUserId();
        Page<ApplicationListResponse> result = applicationService.getCandidateApplications(
                candidateId, status, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationDetail(@PathVariable Long id) {
        return ResponseEntity.ok("Chi tiết đơn");
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> getApplicationTimeline(@PathVariable Long id) {
        return ResponseEntity.ok("Timeline đơn ứng tuyển");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawApplication(@PathVariable Long id) {
        return ResponseEntity.ok("Rút hồ sơ thành công");
    }

    @GetMapping("/cv/application/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER')")
    public ResponseEntity<?> getApplicationCV(@PathVariable Long id) {
        return ResponseEntity.ok("CV của đơn");
    }
}
