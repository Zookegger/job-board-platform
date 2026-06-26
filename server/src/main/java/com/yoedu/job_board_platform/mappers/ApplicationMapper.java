package com.yoedu.job_board_platform.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.dtos.application.EmployerApplicationListResponse;
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

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobSlug", source = "job.slug")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "companyName", source = "job.company.companyName")
    @Mapping(target = "companyLogoUrl", source = "job.company.logoUrl")
    @Mapping(target = "jobLocation", source = "job.location")
    ApplicationResponse toDetailResponse(Application application);

    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateName", source = "candidate.fullName")
    @Mapping(target = "candidateAvatarUrl", source = "candidate.avatarUrl")
    @Mapping(target = "candidateEmail", source = "candidate.user.email")
    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    EmployerApplicationListResponse toEmployerListResponse(Application application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resumeUrl", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    Application toEntity(ApplicationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resumeUrl", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ApplicationRequest request, @MappingTarget Application application);
}
