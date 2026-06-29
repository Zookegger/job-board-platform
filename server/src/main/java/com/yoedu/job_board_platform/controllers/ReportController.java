package com.yoedu.job_board_platform.controllers;

import com.yoedu.job_board_platform.security.AuthorizationConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.ReportApi;
import com.yoedu.job_board_platform.dtos.report.CreateReportRequest;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.services.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller xử lý API báo cáo vi phạm.
 * Triển khai các endpoint được định nghĩa trong {@link ReportApi}.
 */
@RestController
@RequestMapping(ApiPaths.BASE + "/reports")
@RequiredArgsConstructor
public class ReportController implements ReportApi {

    private final ReportService reportService;

    @Override
    @PostMapping
    @PreAuthorize(AuthorizationConstants.AUTHENTICATED)
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.ok(response);
    }
}
