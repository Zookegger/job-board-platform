package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.EmployerJobApi;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobRequest;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.services.JobService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/employer/jobs")
@PreAuthorize(AuthorizationConstants.EMPLOYER)
@RequiredArgsConstructor
public class EmployerJobController implements EmployerJobApi {

    private final JobService jobService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<Page<JobListResponse>> getEmployerJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID employerId = securityUtil.getCurrentUserId();
        Page<JobListResponse> result = jobService.getEmployerJobs(employerId, status, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        UUID employerId = securityUtil.getCurrentUserId();
        JobResponse result = jobService.createJob(employerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobDetail(@PathVariable UUID id) {
        UUID employerId = securityUtil.getCurrentUserId();
        JobResponse result = jobService.getJobDetail(id, employerId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable UUID id, @Valid @RequestBody JobRequest request) {
        UUID employerId = securityUtil.getCurrentUserId();
        JobResponse result = jobService.updateJob(id, employerId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse> submitForReview(@PathVariable UUID id) {
        UUID employerId = securityUtil.getCurrentUserId();
        jobService.submitForReview(id, employerId);
        return ResponseEntity.ok(new ApiResponse("Gửi duyệt thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteJob(@PathVariable UUID id) {
        UUID employerId = securityUtil.getCurrentUserId();
        jobService.deleteJob(id, employerId);
        return ResponseEntity.ok(new ApiResponse("Xóa tin tuyển dụng thành công"));
    }

}
