package com.yoedu.job_board_platform.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.PublicJobApi;
import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.services.JobService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/public")
@RequiredArgsConstructor
public class PublicJobController implements PublicJobApi {

    private final JobService jobService;

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobListResponse>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(jobService.getActiveJobs(page, size));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> getJobDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getActiveJobDetail(id));
    }
}
