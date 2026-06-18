package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.AdminApi;
import com.yoedu.job_board_platform.dtos.admin.AdminSkillResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillRequest;
import com.yoedu.job_board_platform.mappers.SkillMapper;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.services.AdminService;
import com.yoedu.job_board_platform.services.SkillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController implements AdminApi {
    private final AdminService adminService;
    private final SkillService skillService;
    private final SkillMapper skillMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok("Dashboard thống kê");
    }

    // ================ Users ================

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sách user");
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

    @GetMapping("/companies/pending")
    public ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean hasTaxCode,
            @RequestParam(required = false) Boolean hasContact,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(adminService.getPendingCompanies(
                page,
                size,
                keyword,
                hasTaxCode,
                hasContact,
                sortBy,
                direction));
    }

    @PostMapping("/companies/{id}/approve")
    public ResponseEntity<ApiResponse> approveCompany(@PathVariable UUID id) {
        adminService.approveCompany(id);
        return ResponseEntity.ok(new ApiResponse("Duyệt công ty thành công"));
    }

    @PostMapping("/companies/{id}/reject")
    public ResponseEntity<ApiResponse> rejectCompany(@PathVariable UUID id, @RequestParam String reason) {
        adminService.rejectCompany(id, reason);
        return ResponseEntity.ok(new ApiResponse("Từ chối công ty thành công"));
    }

    // ================ Jobs ================

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping("/jobs/{id}/approve")
    public ResponseEntity<?> approveJob(@PathVariable Long id) {
        return ResponseEntity.ok("Duyệt tin thành công");
    }

    @PostMapping("/jobs/{id}/reject")
    public ResponseEntity<?> rejectJob(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok("Từ chối tin");
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok("Xóa tin thành công");
    }

    // ================ Categories ================

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sách ngành");
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory() {
        return ResponseEntity.ok("Tạo ngành thành công");
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Cập nhật ngành thành công");
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Xóa ngành thành công");
    }

    // ================ Skills ================

    @GetMapping("/skills")
    public ResponseEntity<Page<AdminSkillResponse>> getAllSkills(Pageable pageable,
            SkillFilterRequest request) {
        return ResponseEntity.ok(skillService.getAllSkills(pageable, request).map(skillMapper::toAdminResponse));
    }

    @PostMapping("/skills")
    public ResponseEntity<AdminSkillResponse> createSkill(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(skillMapper.toAdminResponse(skillService.createSkill(request)));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<AdminSkillResponse> updateSkill(
            @PathVariable Integer id,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(skillMapper.toAdminResponse(skillService.updateSkill(id, request)));
    }

    @PatchMapping("/skills/{id}/toggle-status")
    public ResponseEntity<AdminSkillResponse> toggleSkillStatus(@PathVariable Integer id) {
        return ResponseEntity.ok(skillMapper.toAdminResponse(skillService.toggleSkillActive(id)));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ApiResponse> deleteSkill(@PathVariable Integer id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(new ApiResponse("Xóa kỹ năng thành công"));
    }
}
