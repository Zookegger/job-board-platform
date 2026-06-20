package com.yoedu.job_board_platform.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.services.JobCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<JobCategoryResponse> getAllCategories() {
        return jobCategoryRepository.findAll().stream()
                .map(c -> new JobCategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @Override
    @Transactional
    public JobCategoryResponse createCategory(JobCategoryRequest request) {
        if (jobCategoryRepository.findByName(request.name()).isPresent()) {
            throw new BadRequestException("Tên ngành nghề đã tồn tại");
        }
        JobCategory saved = jobCategoryRepository.save(
                JobCategory.builder().name(request.name().trim()).build());
        return new JobCategoryResponse(saved.getId(), saved.getName());
    }

    @Override
    @Transactional
    public JobCategoryResponse updateCategory(Integer id, JobCategoryRequest request) {
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành nghề"));
        category.setName(request.name().trim());
        JobCategory saved = jobCategoryRepository.save(category);
        return new JobCategoryResponse(saved.getId(), saved.getName());
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        if (!jobCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy ngành nghề");
        }
        jobCategoryRepository.deleteById(id);
    }
}
