package com.yoedu.job_board_platform.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.User;

@Mapper(componentModel = "spring")
/**
 * MapStruct mapper cho Company entity.
 * Chuyển đổi CompanyRegisterRequest thành User và Company entity khi đăng ký nhà tuyển dụng.
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
    // TODO: phone + email sẽ được cập nhật qua form trong Employer Dashboard (PUT /api/jobs/my-company)
    Company toEntity(CompanyRegisterRequest request);
}
