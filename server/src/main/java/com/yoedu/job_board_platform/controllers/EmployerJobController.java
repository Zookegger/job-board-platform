package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.EmployerJobApi;

@RestController
@RequestMapping(ApiPaths.BASE + "/jobs")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerJobController implements EmployerJobApi {

    @GetMapping
    public ResponseEntity<?> getMyJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping
    public ResponseEntity<?> createJob() {
        return ResponseEntity.ok("Tạo tin thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobDetail(@PathVariable Long id) {
        return ResponseEntity.ok("Chi tiết tin");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id) {
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        return ResponseEntity.ok("Xóa thành công");
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleJobStatus(@PathVariable Long id) {
        return ResponseEntity.ok("Thay đổi trạng thái thành công");
    }

    @GetMapping("/my-company")
    public ResponseEntity<?> getMyCompany() {
        return ResponseEntity.ok("Thông tin công ty");
    }

    @PutMapping("/my-company")
    public ResponseEntity<?> updateMyCompany() {
        return ResponseEntity.ok("Cập nhật công ty thành công");
    }

    @GetMapping("/employer-dashboard")
    public ResponseEntity<?> getEmployerDashboard() {
        return ResponseEntity.ok("Dashboard thống kê");
    }
}
