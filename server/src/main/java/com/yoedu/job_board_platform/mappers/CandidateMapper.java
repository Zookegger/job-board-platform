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
    @Mapping(target = "role", constant = "JOB_SEEKER")
    User toUser(CandidateRegisterRequest request);

    @AfterMapping
    default void createProfile(CandidateRegisterRequest request, @MappingTarget User user) {
        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.fullName())
                .build();
        user.setProfile(profile);
    }
}
