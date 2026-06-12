package com.yoedu.job_board_platform.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.models.Profile;

@Mapper(componentModel = "spring")
/**
 * MapStruct mapper cho Profile entity.
 * Hỗ trợ chuyển đổi Profile thành CandidateProfileResponse, EmployerProfileResponse,
 * và cập nhật entity từ request DTO.
 */
public interface ProfileMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", source = "user.role")
    CandidateProfileResponse toCandidateResponse(Profile profile);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "companyId", source = "employerDetail.company.id")
    @Mapping(target = "companyName", source = "employerDetail.company.companyName")
    @Mapping(target = "roleInCompany", source = "employerDetail.roleInCompany")
    @Mapping(target = "logoUrl", source = "employerDetail.company.logoUrl")
    EmployerProfileResponse toEmployerResponse(Profile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "candidateDetail", ignore = true)
    @Mapping(target = "employerDetail", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCandidateEntity(CandidateProfileRequest request, @MappingTarget Profile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "candidateDetail", ignore = true)
    @Mapping(target = "employerDetail", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployerEntity(EmployerProfileRequest request, @MappingTarget Profile profile);
}
