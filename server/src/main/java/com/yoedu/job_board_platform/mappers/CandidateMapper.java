package com.yoedu.job_board_platform.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", constant = "CANDIDATE")
    User toUser(CandidateRegisterRequest request);

    /**
     * Tạo hồ sơ (Profile) cho người dùng sau khi ánh xạ các trường cơ bản.
     * Phương thức này được gọi tự động sau {@link #toUser(CandidateRegisterRequest)},
     * dùng để xây dựng đối tượng {@link Profile} với tên đầy đủ từ request
     * và liên kết nó với đối tượng {@link User} đã được ánh xạ.
     *
     * @param request thông tin đăng ký từ client chứa họ tên
     * @param user    đối tượng User đích đã được map một phần, cần gán profile
     */
    @AfterMapping
    default void createProfile(CandidateRegisterRequest request, @MappingTarget User user) {
        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.fullName())
                .build();
        user.setProfile(profile);
    }
}
