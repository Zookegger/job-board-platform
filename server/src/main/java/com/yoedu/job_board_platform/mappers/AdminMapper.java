package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "id", source = "company.id")
    @Mapping(target = "isApproved", source = "company.approved")
    @Mapping(target = "employerName", source = "detail.profile.fullName")
    @Mapping(target = "employerEmail", source = "detail.profile.user.email")
    @Mapping(target = "employerPhone", source = "detail.profile.phone")
    @Mapping(target = "roleInCompany", source = "detail.roleInCompany")
    PendingCompanyResponse toPendingCompanyResponse(Company company, CompanyEmployerDetail detail);
}
