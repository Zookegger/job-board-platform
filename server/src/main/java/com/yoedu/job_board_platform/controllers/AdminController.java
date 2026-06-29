
package com.yoedu.job_board_platform.controllers;

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.AdminApi;
import com.yoedu.job_board_platform.dtos.admin.*;
import com.yoedu.job_board_platform.dtos.report.AdminReportActionRequest;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillRequest;
import com.yoedu.job_board_platform.mappers.AdminMapper;
import com.yoedu.job_board_platform.mappers.SkillMapper;
import com.yoedu.job_board_platform.models.ReportStatus;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.AdminService;
import com.yoedu.job_board_platform.services.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.BASE + "/admin")
@PreAuthorize(AuthorizationConstants.ADMIN)
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final SkillService skillService;
    private final SkillMapper skillMapper;
    private final SkillRepository skillRepository;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/statistics/applications-chart")
    public ResponseEntity<AdminApplicationChartResponse> getApplicationChartStats(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(adminService.getApplicationChartStats(days));
    }

    // ================ Users ================

    @Override
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getUserStats(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(adminService.getUsers(role, isActive, pageable).map(adminMapper::toAdminUserResponse));
    }

    @GetMapping("/users/stats")
    public ResponseEntity<?> getUserStats() {
        return ResponseEntity.ok("Thống kê user");
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Khóa tài khoản thành công");
    }

    @PostMapping("/users/{id}/reactivate")
    public ResponseEntity<?> reactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Mở khóa thành công");
    }

    // ================ Companies ================

    @GetMapping("/companies")
    public ResponseEntity<Page<AdminCompanyListResponse>> getAllCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllCompanies(keyword, status, pageable));
    }

    @GetMapping("/companies/pending")
    public ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasTaxCode,
            @RequestParam(required = false) Boolean hasContact,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingCompanies(
                keyword, hasTaxCode, hasContact, pageable));
    }

    @PatchMapping("/companies/{id}/approve")
    public ResponseEntity<ApiResponse> approveCompany(@PathVariable UUID id) {
        adminService.approveCompany(id);
        return ResponseEntity.ok(new ApiResponse("Duyệt công ty thành công"));
    }

    @PatchMapping("/companies/{id}/reject")
    public ResponseEntity<ApiResponse> rejectCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyRejectionRequest request) {
        adminService.rejectCompany(id, request);
        return ResponseEntity.ok(new ApiResponse("Từ chối công ty thành công"));
    }

    @PatchMapping("/companies/{id}/suspend")
    public ResponseEntity<ApiResponse> suspendCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CompanySuspensionRequest request) {
        adminService.suspendCompany(id, request);
        return ResponseEntity.ok(new ApiResponse("Tạm ngưng công ty thành công"));
    }

    @PatchMapping("/companies/{id}/unsuspend")
    public ResponseEntity<ApiResponse> unsuspendCompany(@PathVariable UUID id) {
        adminService.unsuspendCompany(id);
        return ResponseEntity.ok(new ApiResponse("Mở tạm ngưng công ty thành công"));
    }

    // ================ Jobs ================

    @GetMapping("/jobs")
    public ResponseEntity<Page<AdminJobListResponse>> getAllJobs(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllJobs(status, pageable));
    }

    @GetMapping("/jobs/pending")
    public ResponseEntity<Page<PendingJobResponse>> getPendingJobs(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingJobs(pageable));
    }

    @PatchMapping("/jobs/{id}/approve")
    public ResponseEntity<ApiResponse> approveJob(@PathVariable UUID id) {
        adminService.approveJob(id);
        return ResponseEntity.ok(new ApiResponse("Duyệt tin tuyển dụng thành công"));
    }

    @PatchMapping("/jobs/{id}/reject")
    public ResponseEntity<ApiResponse> rejectJob(
            @PathVariable UUID id,
            @Valid @RequestBody JobRejectRequest request) {
        adminService.rejectJob(id, request.reason());
        return ResponseEntity.ok(new ApiResponse("Từ chối tin tuyển dụng thành công"));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse> deleteJob(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(new ApiResponse("Xóa tin thành công"));
    }

    // ================ Reports ================

    @GetMapping("/reports")
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(adminService.getReports(status, pageable));
    }

    @PatchMapping("/reports/{id}/review")
    public ResponseEntity<ApiResponse> reviewReport(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminReportActionRequest request) {
        String reviewNotes = request != null ? request.reviewNotes() : null;
        adminService.reviewReport(id, reviewNotes);
        return ResponseEntity.ok(new ApiResponse("Duyệt báo cáo thành công"));
    }

    @PatchMapping("/reports/{id}/dismiss")
    public ResponseEntity<ApiResponse> dismissReport(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminReportActionRequest request) {
        String reviewNotes = request != null ? request.reviewNotes() : null;
        adminService.dismissReport(id, reviewNotes);
        return ResponseEntity.ok(new ApiResponse("Gỡ bỏ báo cáo thành công"));
    }

    @PatchMapping("/reports/{id}/resolve")
    public ResponseEntity<ApiResponse> resolveReport(
            @PathVariable UUID id,
            @RequestBody(required = false) AdminReportActionRequest request) {
        String reviewNotes = request != null ? request.reviewNotes() : null;
        adminService.resolveReport(id, reviewNotes);
        return ResponseEntity.ok(new ApiResponse("Giải quyết báo cáo thành công"));
    }

    // ================ Skills ================

    @GetMapping("/skills")
    public ResponseEntity<Page<AdminSkillResponse>> getAllSkills(Pageable pageable,
            SkillFilterRequest request) {
        return ResponseEntity.ok(skillService.getAllSkills(pageable, request).map(skillMapper::toAdminResponse));
    }

    @PostMapping("/skills")
    public ResponseEntity<AdminSkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        var response = skillService.createSkill(request);
        Skill skill = skillRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("Skill not found after creation"));
        return ResponseEntity.ok(skillMapper.toAdminResponse(skill));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<AdminSkillResponse> updateSkill(
            @PathVariable Integer id,
            @Valid @RequestBody SkillRequest request) {
        var response = skillService.updateSkill(id, request);
        Skill skill = skillRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("Skill not found after update"));
        return ResponseEntity.ok(skillMapper.toAdminResponse(skill));
    }

    @PatchMapping("/skills/{id}/toggle-status")
    public ResponseEntity<AdminSkillResponse> toggleSkillStatus(@PathVariable Integer id) {
        var response = skillService.toggleSkillActive(id);
        Skill skill = skillRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("Skill not found after toggle"));
        return ResponseEntity.ok(skillMapper.toAdminResponse(skill));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ApiResponse> deleteSkill(@PathVariable Integer id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(new ApiResponse("Xóa kỹ năng thành công"));
    }
}
