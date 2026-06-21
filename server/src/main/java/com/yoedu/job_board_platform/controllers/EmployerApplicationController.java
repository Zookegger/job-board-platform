package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.EmployerApplicationApi;
import com.yoedu.job_board_platform.security.AuthorizationConstants;

@RestController
@RequestMapping(ApiPaths.BASE + "/employer/applications")
@PreAuthorize(AuthorizationConstants.EMPLOYER)
public class EmployerApplicationController implements EmployerApplicationApi {

    @GetMapping
    public ResponseEntity<?> getApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sách ứng viên");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationDetail(@PathVariable Long id) {
        return ResponseEntity.ok("Chi tiết hồ sơ");
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
