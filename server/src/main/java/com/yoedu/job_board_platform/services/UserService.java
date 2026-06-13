package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UpdateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;

/**
 * Service quản lý người dùng.
 * Cung cấp chức năng tạo và cập nhật thông tin người dùng.
 */
public interface UserService {

    /**
     * Tạo mới một người dùng dựa trên thông tin từ CreateUserRequest.
     *
     * @param request thông tin người dùng cần tạo
     * @return UserResponse thông tin người dùng đã được tạo
     * @throws ConflictException nếu email đã tồn tại
     */
    UserResponse create(CreateUserRequest request);

    /**
     * Cập nhật thông tin người dùng dựa trên id và thông tin từ UpdateUserRequest.
     *
     * @param id      id của người dùng cần cập nhật
     * @param request thông tin cập nhật
     * @return UserResponse thông tin người dùng đã được cập nhật
     * @throws BadRequestException nếu email đã tồn tại hoặc không tìm thấy người dùng
     */
    UserResponse update(UUID id, UpdateUserRequest request);
}
