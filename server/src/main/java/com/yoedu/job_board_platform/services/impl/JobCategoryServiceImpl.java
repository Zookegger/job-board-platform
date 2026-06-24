package com.yoedu.job_board_platform.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.mappers.JobCategoryMapper;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.services.JobCategoryService;
import com.yoedu.job_board_platform.specifications.JobCategorySpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;
    private final JobCategoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobCategory> getAllCategories() {
        return jobCategoryRepository.findAll();
    }

    @Override
    @Transactional
    public JobCategory createCategory(JobCategoryRequest request) {
        if (jobCategoryRepository.findByName(request.name()).isPresent()) {
            throw new BadRequestException("Tên ngành nghề đã tồn tại");
        }
        var jobCategory = mapper.toEntity(request);

        return jobCategoryRepository.save(jobCategory);
    }

    @Override
    @Transactional
    public JobCategory updateCategory(Integer id, JobCategoryRequest request) {
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngành nghề"));
        category.setName(request.name().trim());
        return jobCategoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        if (!jobCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy ngành nghề");
        }
        jobCategoryRepository.deleteById(id);
    }

    @Override
    public Page<JobCategory> getAllCategoriesPage(String keyword, Pageable pageable) {
        Specification<JobCategory> specification = Specification.where(JobCategorySpecification.hasKeyword(keyword));

        return jobCategoryRepository.findAll(specification, pageable);
    }
}
