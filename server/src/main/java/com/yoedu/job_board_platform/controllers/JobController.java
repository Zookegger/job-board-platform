package com.yoedu.job_board_platform.controllers;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.JobApi;
import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.dtos.job.JobSearchRequest;
import com.yoedu.job_board_platform.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.BASE + "/jobs")
@RequiredArgsConstructor
public class JobController implements JobApi {

    private final JobService jobService;

    @GetMapping("/public")
    @Override
    public ResponseEntity<Page<JobListResponse>> searchPublicJobs(
            @ParameterObject JobSearchRequest request,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(jobService.searchPublicJobs(request, pageable));
    }

    @GetMapping("/public/{slug}")
    @Override
    public ResponseEntity<JobResponse> getPublicJobDetail(@PathVariable String slug) {
        return ResponseEntity.ok(jobService.getPublicJobDetail(slug));
    }

    @GetMapping("/{id}/related")
    @Override
    public ResponseEntity<Page<JobListResponse>> getRelatedJobs(@PathVariable UUID id, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(jobService.getRelatedJobs(id, pageable));
    }
}