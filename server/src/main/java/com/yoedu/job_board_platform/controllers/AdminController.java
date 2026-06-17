package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.dtos.company.CompanyApprovalRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyRejectionRequest;
import com.yoedu.job_board_platform.dtos.company.CompanySuspensionRequest;
import com.yoedu.job_board_platform.dtos.company.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.services.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(ApiPaths.BASE + "/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin", description = "API quản trị hệ thống")
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Lấy thông tin dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok("Dashboard thống kê");
    }

    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách người dùng")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách user");
    }

    @GetMapping("/users/stats")
    @Operation(summary = "Lấy thống kê người dùng")
    public ResponseEntity<?> getUserStats() {
        return ResponseEntity.ok("Thống kê user");
    }

    @PostMapping("/users/{id}/suspend")
    @Operation(summary = "Khóa tài khoản người dùng")
    public ResponseEntity<?> suspendUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Khóa tài khoản thành công");
    }

    @PostMapping("/users/{id}/reactivate")
    @Operation(summary = "Mở khóa tài khoản người dùng")
    public ResponseEntity<?> reactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok("Mở khóa thành công");
    }

    @GetMapping("/companies/pending")
    @Operation(
            summary = "Lấy danh sách công ty chờ duyệt",
            description = "Lấy danh sách tất cả các công ty có trạng thái PENDING",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Danh sách công ty chờ duyệt",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<Page<PendingCompanyResponse>> getPendingCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PendingCompanyResponse> companies = adminService.getPendingCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    @PostMapping("/companies/{id}/approve")
    @Operation(
            summary = "Phê duyệt công ty",
            description = "Phê duyệt một công ty và cho phép họ đăng tuyển",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Phê duyệt thành công"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty"),
                    @ApiResponse(responseCode = "403", description = "Không có quyền")
            }
    )
    public ResponseEntity<?> approveCompany(
            @PathVariable @NotNull UUID id,
            @RequestBody(required = false) CompanyApprovalRequest request
    ) {
        log.info("Approving company with ID: {}", id);
        adminService.approveCompany(id);
        return ResponseEntity.ok(new ResponseMessage("Duyệt công ty thành công"));
    }

    @PostMapping("/companies/{id}/reject")
    @Operation(
            summary = "Từ chối công ty",
            description = "Từ chối phê duyệt một công ty và thông báo lý do",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Từ chối thành công"),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty"),
                    @ApiResponse(responseCode = "403", description = "Không có quyền")
            }
    )
    public ResponseEntity<?> rejectCompany(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid CompanyRejectionRequest request
    ) {
        log.info("Rejecting company with ID: {}, reason: {}", id, request.rejectionReason());
        adminService.rejectCompany(id, request.rejectionReason());
        return ResponseEntity.ok(new ResponseMessage("Từ chối công ty thành công"));
    }

    @PostMapping("/companies/{id}/suspend")
    @Operation(
            summary = "Tạm ngưng công ty",
            description = "Tạm ngưng hoạt động của một công ty do vi phạm",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tạm ngưng thành công"),
                    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                    @ApiResponse(responseCode = "404", description = "Không tìm thấy công ty"),
                    @ApiResponse(responseCode = "403", description = "Không có quyền")
            }
    )
    public ResponseEntity<?> suspendCompany(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid CompanySuspensionRequest request
    ) {
        log.info("Suspending company with ID: {}, reason: {}", id, request.suspensionReason());
        adminService.suspendCompany(id, request.suspensionReason());
        return ResponseEntity.ok(new ResponseMessage("Tạm ngưng công ty thành công"));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Lấy danh sách tin tuyển dụng")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping("/jobs/{id}/approve")
    @Operation(summary = "Phê duyệt tin tuyển dụng")
    public ResponseEntity<?> approveJob(@PathVariable Long id) {
        return ResponseEntity.ok("Duyệt tin thành công");
    }

    @PostMapping("/jobs/{id}/reject")
    @Operation(summary = "Từ chối tin tuyển dụng")
    public ResponseEntity<?> rejectJob(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok("Từ chối tin");
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Xóa tin tuyển dụng")
    public ResponseEntity<?> deleteJob(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok("Xóa tin thành công");
    }

    @GetMapping("/categories")
    @Operation(summary = "Lấy danh sách ngành")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok("Danh sách ngành");
    }

    @PostMapping("/categories")
    @Operation(summary = "Tạo ngành mới")
    public ResponseEntity<?> createCategory() {
        return ResponseEntity.ok("Tạo ngành thành công");
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Cập nhật ngành")
    public ResponseEntity<?> updateCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Cập nhật ngành thành công");
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Xóa ngành")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok("Xóa ngành thành công");
    }

    public record ResponseMessage(String message) {
    }
}