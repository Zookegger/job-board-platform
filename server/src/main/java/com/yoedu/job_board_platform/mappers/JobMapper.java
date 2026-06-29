package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.admin.AdminJobListResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingJobResponse;
import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobRequest;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.models.Job;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "company", ignore = true)
	@Mapping(target = "category", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "postedDate", ignore = true)
	@Mapping(target = "expirationDate", ignore = true)
	@Mapping(target = "rejectionReason", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Job toEntity(JobRequest request);

	@Mapping(target = "companyId", source = "company.id")
	@Mapping(target = "companyName", source = "company.companyName")
	@Mapping(target = "companySlug", source = "company.slug")
	@Mapping(target = "categoryId", source = "category.id")
	@Mapping(target = "categoryName", source = "category.name")
	@Mapping(target = "skills", ignore = true)
	@Mapping(target = "withSkills", ignore = true)
	@Mapping(target = "companyLogoUrl", source = "company.logoUrl")
	@Mapping(target = "companyAddress", source = "company.address")
	JobResponse toResponse(Job job);

	@Mapping(target = "companyName", source = "company.companyName")
	@Mapping(target = "companyLogoUrl", source = "company.logoUrl")
	@Mapping(target = "categoryName", source = "category.name")
	AdminJobListResponse toAdminJobListResponse(Job job);

	@Mapping(target = "companyName", source = "company.companyName")
	@Mapping(target = "companyLogoUrl", source = "company.logoUrl")
	JobListResponse toSummary(Job job);

	@Mapping(target = "companyName", source = "job.company.companyName")
	@Mapping(target = "companyLogoUrl", source = "job.company.logoUrl")
	@Mapping(target = "categoryName", source = "job.category.name")
	PendingJobResponse toPendingJobResponse(Job job);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "company", ignore = true)
	@Mapping(target = "category", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "postedDate", ignore = true)
	@Mapping(target = "expirationDate", ignore = true)
	@Mapping(target = "rejectionReason", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntity(JobRequest request, @MappingTarget Job job);
}
