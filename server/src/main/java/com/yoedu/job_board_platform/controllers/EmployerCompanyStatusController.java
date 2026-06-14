package com.yoedu.job_board_platform.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.services.CompanyStatusService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employer/company")
@RequiredArgsConstructor
/**
 * Controller cho employer theo dõi trạng thái phê duyệt công ty của mình.
 */
public class EmployerCompanyStatusController {

    private final CompanyStatusService companyStatusService;
    private final SecurityUtil securityUtil;

    /**
     * GET /api/employer/company/status
     * Trả về trạng thái phê duyệt hiện tại của công ty.
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<CompanyStatusResponse> getStatus() {
        UUID employerId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(companyStatusService.getStatusByEmployerId(employerId));
    }

    /**
     * GET /api/employer/company/approval-history
     * Trả về lịch sử phê duyệt của công ty, sắp xếp mới nhất lên đầu.
     */
    @GetMapping("/approval-history")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<ApprovalLogResponse>> getApprovalHistory() {
        UUID employerId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(companyStatusService.getHistoryByEmployerId(employerId));
    }
}
