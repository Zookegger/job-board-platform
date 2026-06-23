package com.yoedu.job_board_platform.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;
import com.yoedu.job_board_platform.models.JobCategory;

@Mapper(componentModel = "spring")
public interface JobCategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    JobCategory toEntity(JobCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    void updateEntity(JobCategoryRequest request, @MappingTarget JobCategory category);

    JobCategoryResponse toResponse(JobCategory jobCategory);

    List<JobCategoryResponse> toResponseList(List<JobCategory> jobCategory);
}
