package com.yoedu.job_board_platform.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UpdateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.models.User;

@Mapper(componentModel = "spring")
/**
 * MapStruct mapper cho User entity.
 * Chuyển đổi giữa User entity, CreateUserRequest, UpdateUserRequest và
 * UserResponse.
 */
public interface UserMapper {
    @Mapping(target = "role", source = "role")
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "avatarUrl", source = "profile.avatarUrl")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Cập nhật thông tin người dùng từ UpdateUserRequest vào entity User.
     * Các trường null trong request được bỏ qua để giữ nguyên giá trị hiện tại.
     *
     * @param request thông tin người dùng cần cập nhật
     * @param user    entity User cần được cập nhật (bị mutate)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);
}
