package com.yoedu.job_board_platform.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "email", source = "userEmail")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", constant = "EMPLOYER")
    User toUser(CompanyRegisterRequest request);

    /**
     * Tạo hồ sơ (Profile) cho người dùng đại diện công ty sau khi ánh xạ các trường
     * cơ bản.
     * Phương thức này được gọi tự động sau {@link #toUser(CompanyRegisterRequest)},
     * dùng để xây dựng đối tượng {@link Profile} với tên đầy đủ từ request
     * và liên kết nó với đối tượng {@link User} đã được ánh xạ.
     *
     * @param request thông tin đăng ký từ client chứa họ tên
     * @param user    đối tượng User đích đã được map một phần, cần gán profile
     */
    @AfterMapping
    default void createProfile(CompanyRegisterRequest request, @MappingTarget User user) {
        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.fullName())
                .phone(request.userPhone())
                .build();
        user.setProfile(profile);
    }

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
