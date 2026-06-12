package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UpdateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;

/**
 * Service quản lý người dùng.
 * Cung cấp chức năng tạo và cập nhật thông tin người dùng.
 */
public interface UserService {
    UserResponse create(CreateUserRequest request);
    UserResponse update(UUID id, UpdateUserRequest request);
}
