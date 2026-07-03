package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.EmployerDashboardApi;
import com.yoedu.job_board_platform.dtos.employer.EmployerDashboardStatsResponse;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.EmployerDashboardService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/employer/dashboard")
@PreAuthorize(AuthorizationConstants.EMPLOYER)
@RequiredArgsConstructor
public class EmployerDashboardController implements EmployerDashboardApi {

    private final EmployerDashboardService employerDashboardService;
    private final SecurityUtil securityUtil;

    @GetMapping("/stats")
    public ResponseEntity<EmployerDashboardStatsResponse> getDashboardStats() {
        UUID employerId = securityUtil.getCurrentUserId();
        EmployerDashboardStatsResponse stats = employerDashboardService.getStats(employerId);
        return ResponseEntity.ok(stats);
    }
}
