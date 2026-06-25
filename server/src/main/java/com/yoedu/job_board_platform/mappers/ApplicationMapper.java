package com.yoedu.job_board_platform.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.models.Application;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobSlug", source = "job.slug")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.company.companyName")
    @Mapping(target = "companyLogoUrl", source = "job.company.logoUrl")
    @Mapping(target = "jobLocation", source = "job.location")
    ApplicationListResponse toListResponse(Application application);
}
