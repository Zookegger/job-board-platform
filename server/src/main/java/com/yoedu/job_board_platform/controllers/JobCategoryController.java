package com.yoedu.job_board_platform.controllers;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.common.ApiResponse;
import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.JobCategoryApi;
import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;
import com.yoedu.job_board_platform.services.JobCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/categories")
@RequiredArgsConstructor
public class JobCategoryController implements JobCategoryApi {

    private final JobCategoryService jobCategoryService;

    @GetMapping
    public ResponseEntity<List<JobCategoryResponse>> getCategories() {
        return ResponseEntity.ok(jobCategoryService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobCategoryResponse> createCategory(@Valid @RequestBody JobCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobCategoryResponse> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody JobCategoryRequest request) {
        return ResponseEntity.ok(jobCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Integer id) {
        jobCategoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse("Xóa ngành nghề thành công"));
    }
}
