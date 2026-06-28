package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.dtos.company.*;
import com.yoedu.job_board_platform.dtos.job.JobCategoryResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyApprovalLog;
import com.yoedu.job_board_platform.models.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "email", source = "userEmail")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", constant = "EMPLOYER")
    User toUser(CompanyRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "website", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isApproved", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "suspensionReason", ignore = true)
    @Mapping(target = "reviewReason", ignore = true)
    Company toEntity(CompanyRegisterRequest request);

    @Mapping(target = "isApproved", source = "approved")
    CompanyResponse toResponse(Company company);

    List<CompanyResponse> toResponseList(List<Company> company);

    @Mapping(target = "totalOpenJobs", ignore = true)
    @Mapping(target = "name", source = "companyName")
    @Mapping(target = "categories", ignore = true)
    PublicCompanyResponse toPublicResponse(Company company);

    @Mapping(target = "name", source = "company.companyName")
    @Mapping(target = "totalOpenJobs", source = "jobCount")
    @Mapping(target = "categories", ignore = true)
    PublicCompanyResponse toPublicResponse(Company company, long jobCount);

    @Mapping(target = "name", source = "company.companyName")
    @Mapping(target = "totalOpenJobs", source = "jobCount")
    @Mapping(target = "categories", source = "categories")
    PublicCompanyResponse toPublicResponse(Company company, long jobCount, List<JobCategoryResponse> categories);

    List<PublicCompanyResponse> toPublicResponseList(List<Company> company);

    @Mapping(target = "name", source = "company.companyName")
    @Mapping(target = "totalOpenJobs", source = "jobCount")
    @Mapping(target = "categories", source = "categories")
    PublicCompanyListResponse toPublicListResponse(Company company, long jobCount,
            List<JobCategoryResponse> categories);

    /**
     * Chuyển đổi Company entity thành CompanyStatusResponse cho employer.
     * approvalStatus ← status, name ← companyName, submittedAt ← createdAt,
     * reviewNote ← rejectionReason, reviewedAt ← approvedAt.
     */
    @Mapping(target = "companyId", source = "id")
    @Mapping(target = "name", source = "companyName")
    @Mapping(target = "approvalStatus", source = "status")
    @Mapping(target = "submittedAt", source = "createdAt")
    @Mapping(target = "reviewNote", source = "rejectionReason")
    @Mapping(target = "reviewedAt", source = "approvedAt")
    CompanyStatusResponse toStatusResponse(Company company);

    /**
     * Cập nhật thông tin công ty từ CompanyRequest vào entity Company.
     * Các trường null trong request được bỏ qua để giữ nguyên giá trị hiện tại.
     *
     * @param request thông tin công ty cần cập nhật
     * @param company entity Company cần được cập nhật (bị mutate)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "suspensionReason", ignore = true)
    @Mapping(target = "reviewReason", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CompanyRequest request, @MappingTarget Company company);

    @Mapping(target = "actorId", source = "actor.id")
    ApprovalLogResponse toApprovalLogResponse(CompanyApprovalLog log);
}
