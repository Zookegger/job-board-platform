package com.yoedu.job_board_platform.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.CompanyApi;
import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.CompanyService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE + "/company")
/**
 * Controller xử lý các API liên quan đến thông tin công ty.
 * Hỗ trợ xem, cập nhật thông tin công ty và tra cứu công ty từ bài đăng tuyển
 * dụng.
 */
public class CompanyController implements CompanyApi {
    private final CompanyService companyService;
    private final SecurityUtil securityUtil;

    @Override
    @GetMapping("/employer")
    /**
     * Lấy thông tin công ty của nhà tuyển dụng hiện tại.
     * Yêu cầu xác thực — người dùng phải đăng nhập.
     *
     * @return CompanyResponse thông tin công ty
     */
    public ResponseEntity<CompanyResponse> findCompanyByEmployerId() {
        UUID employerId = securityUtil.getCurrentUserId();
        CompanyResponse company = companyService.findCompanyByEmployerId(employerId);
        return ResponseEntity.ok(company);
    }

    @Override
    @PutMapping
    @PreAuthorize(AuthorizationConstants.EMPLOYER)
    /**
     * Cập nhật thông tin công ty của nhà tuyển dụng hiện tại.
     * Các trường null trong request được bỏ qua (partial update).
     * Yêu cầu role EMPLOYER.
     *
     * @param request thông tin công ty cần cập nhật
     * @return CompanyResponse thông tin công ty sau khi cập nhật
     */
    public ResponseEntity<CompanyResponse> update(@Valid @RequestBody CompanyRequest request) {
        UUID employerId = securityUtil.getCurrentUserId();
        CompanyResponse updated = companyService.update(employerId, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    @GetMapping("/job-post")
    /**
     * Lấy thông tin công ty từ bài đăng tuyển dụng.
     * API công khai — không yêu cầu xác thực.
     *
     * @param jobPostId UUID của bài đăng tuyển dụng
     * @return CompanyResponse thông tin công ty
     */
    public ResponseEntity<CompanyResponse> getCompanyByJobPost(UUID jobPostId) {
        return ResponseEntity.ok(companyService.getCompanyByJobPost(jobPostId));
    }

    @Override
    @GetMapping
    /**
     * Lấy danh sách tất cả các công ty trên hệ thống.
     * API công khai — không yêu cầu xác thực.
     *
     * @return danh sách CompanyResponse
     */
    public ResponseEntity<List<CompanyResponse>> listCompanies() {
        return ResponseEntity.ok(companyService.listCompanies());
    }

    @Override
    @GetMapping("/status")
    @PreAuthorize(AuthorizationConstants.EMPLOYER)
    /**
     * Lấy trạng thái phê duyệt hiện tại của công ty.
     * Yêu cầu role EMPLOYER.
     *
     * @return CompanyStatusResponse trạng thái phê duyệt
     */
    public ResponseEntity<CompanyStatusResponse> getStatus() {
        UUID employerId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(companyService.getStatusByEmployerId(employerId));
    }

    @Override
    @GetMapping("/approval-history")
    @PreAuthorize(AuthorizationConstants.EMPLOYER)
    /**
     * Lấy lịch sử phê duyệt của công ty, sắp xếp mới nhất lên đầu.
     * Yêu cầu role EMPLOYER.
     *
     * @return danh sách ApprovalLogResponse
     */
    public ResponseEntity<List<ApprovalLogResponse>> getApprovalHistory() {
        UUID employerId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(companyService.getHistoryByEmployerId(employerId));
    }
}
