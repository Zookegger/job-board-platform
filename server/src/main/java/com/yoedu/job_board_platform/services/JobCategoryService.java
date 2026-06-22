package com.yoedu.job_board_platform.services;

import java.util.List;

import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;

public interface JobCategoryService {
    List<JobCategoryResponse> getAllCategories();

    JobCategoryResponse createCategory(JobCategoryRequest request);

    JobCategoryResponse updateCategory(Integer id, JobCategoryRequest request);

    void deleteCategory(Integer id);
}
