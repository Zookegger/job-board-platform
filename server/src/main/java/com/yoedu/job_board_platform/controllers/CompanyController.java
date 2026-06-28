package com.yoedu.job_board_platform.controllers;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.CompanyApi;
import com.yoedu.job_board_platform.dtos.company.*;
import com.yoedu.job_board_platform.mappers.CompanyMapper;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.CompanyService;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controller xử lý các API liên quan đến thông tin công ty.
 * Hỗ trợ xem, cập nhật thông tin công ty và tra cứu công ty từ bài đăng tuyển
 * dụng.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE + "/company")
public class CompanyController implements CompanyApi {
    private final CompanyService companyService;
    private final CompanyMapper companyMapper;
    private final SecurityUtil securityUtil;

    /**
     * Lấy thông tin công ty của nhà tuyển dụng hiện tại.
     * Yêu cầu xác thực — người dùng phải đăng nhập.
     *
     * @return CompanyResponse thông tin công ty
     */
    @Override
    @GetMapping("/employer")
    public ResponseEntity<CompanyResponse> findCompanyByEmployerId() {
        UUID employerId = securityUtil.getCurrentUserId();
        CompanyResponse company = companyService.findCompanyByEmployerId(employerId);
        return ResponseEntity.ok(company);
    }

    /**
     * Cập nhật thông tin công ty của nhà tuyển dụng hiện tại.
     * Các trường null trong request được bỏ qua (partial update).
     * Yêu cầu role EMPLOYER.
     *
     * @param request thông tin công ty cần cập nhật
     * @return CompanyResponse thông tin công ty sau khi cập nhật
     */
    @Override
    @PutMapping
    @PreAuthorize(AuthorizationConstants.EMPLOYER)
    public ResponseEntity<CompanyResponse> update(@Valid @RequestBody CompanyRequest request) {
        UUID employerId = securityUtil.getCurrentUserId();
        CompanyResponse updated = companyService.update(employerId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Lấy thông tin công ty từ bài đăng tuyển dụng.
     * API công khai — không yêu cầu xác thực.
     *
     * @param jobPostId UUID của bài đăng tuyển dụng
     * @return CompanyResponse thông tin công ty
     */
    @Override
    @GetMapping("/job-post")
    public ResponseEntity<CompanyResponse> getCompanyByJobPost(UUID jobPostId) {
        return ResponseEntity.ok(companyService.getCompanyByJobPost(jobPostId));
    }

    /**
     * Lấy danh sách tất cả các công ty trên hệ thống.
     * API công khai — không yêu cầu xác thực.
     *
     * @return danh sách CompanyResponse
     */
    @Override
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> listCompanies() {
        return ResponseEntity.ok(companyMapper.toResponseList(companyService.listCompanies()));
    }

    /**
     * Lấy trạng thái phê duyệt hiện tại của công ty.
     * Yêu cầu role EMPLOYER.
     *
     * @return CompanyStatusResponse trạng thái phê duyệt
     */
    @Override
    @GetMapping("/status")
    @PreAuthorize(AuthorizationConstants.EMPLOYER)
    public ResponseEntity<CompanyStatusResponse> getStatus() {
        UUID employerId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(companyService.getStatusByEmployerId(employerId));
    }

    /**
     * Lấy lịch sử phê duyệt của công ty, sắp xếp mới nhất lên đầu.
     * Yêu cầu role EMPLOYER.
     *
     * @return danh sách ApprovalLogResponse
     */
    @Override
    @GetMapping("/approval-history")
    @PreAuthorize(AuthorizationConstants.EMPLOYER)
    public ResponseEntity<List<ApprovalLogResponse>> getApprovalHistory() {
        UUID employerId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(companyService.getHistoryByEmployerId(employerId));
    }

    /**
     * Lấy lịch sử phê duyệt của công ty, sắp xếp mới nhất lên đầu.
     * Yêu cầu role EMPLOYER.
     *
     * @return danh sách ApprovalLogResponse
     */
    @Override
    @GetMapping("/search")
    @PreAuthorize(AuthorizationConstants.ADMIN)
    public ResponseEntity<Page<PublicCompanyResponse>> listCompaniesPage(String keyword,
            Set<Integer> jobCategoryIds,
            Pageable pageable) {

        var companies = companyService.searchAllCompanies(keyword, jobCategoryIds, pageable);

        return ResponseEntity.ok(companies.map(companyMapper::toPublicResponse));
    }
}
