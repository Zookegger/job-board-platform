package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.admin.AdminCompanyListResponse;
import com.yoedu.job_board_platform.dtos.admin.AdminUserResponse;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;

import com.yoedu.job_board_platform.models.User;
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

	@Mapping(target = "isApproved", source = "company.approved")
	AdminCompanyListResponse toAdminCompanyListResponse(Company company);

	@Mapping(target = "fullName", source = "profile.fullName")
	@Mapping(target = "isActive", source = "active")
	AdminUserResponse toAdminUserResponse(User user);

	default PendingCompanyResponse toPendingCompanyResponseSafe(Company company, CompanyEmployerDetail detail) {
		if (detail == null) {
			return new PendingCompanyResponse(
					company.getId(),
					company.getCompanyName(),
					company.getSlug(),
					company.getAddress(),
					company.getDescription(),
					company.getWebsite(),
					company.getLogoUrl(),
					company.getEmail(),
					company.getPhone(),
					company.getTaxCode(),
					company.getStatus(),
					company.isApproved(),
					company.getCreatedAt(),
					company.getApprovedAt(),
					null,
					null,
					null,
					null);
		}
		return toPendingCompanyResponse(company, detail);
	}
}
