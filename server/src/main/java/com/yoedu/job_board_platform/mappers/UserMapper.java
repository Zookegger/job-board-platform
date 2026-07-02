package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UpdateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.models.User;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper cho User entity.
 * Chuyển đổi giữa User entity, CreateUserRequest, UpdateUserRequest và
 * UserResponse.
 */
@Mapper(componentModel = "spring")
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
	@Mapping(target = "updatedAt", ignore = true)
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
	@Mapping(target = "updatedAt", ignore = true)
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntity(UpdateUserRequest request, @MappingTarget User user);
}
