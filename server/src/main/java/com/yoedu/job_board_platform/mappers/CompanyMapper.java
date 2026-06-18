package com.yoedu.job_board_platform.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.User;

@Mapper(componentModel = "spring")
/**
 * MapStruct mapper cho Company entity.
 * Chuyển đổi CompanyRegisterRequest thành User và Company entity khi đăng ký nhà tuyển dụng.
 * Chuyển đổi Company entity thành CompanyResponse và cập nhật từ CompanyRequest.
 */
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
    // TODO: phone + email sẽ được cập nhật qua form trong Employer Dashboard (PUT /api/jobs/my-company)
    Company toEntity(CompanyRegisterRequest request);

    @Mapping(target = "isApproved", source = "approved")
    CompanyResponse toResponse(Company company);

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
}
