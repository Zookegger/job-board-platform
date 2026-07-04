package com.yoedu.job_board_platform.services;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UpdateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;

import java.util.UUID;

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

    /**
     * Vô hiệu hóa tài khoản người dùng theo ID.
     *
     * <p>Người dùng sau khi bị khóa sẽ không thể đăng nhập hoặc sử dụng hệ thống.</p>
     *
     * @param id id của người dùng cần khóa tài khoản
     * @throws BadRequestException nếu không tìm thấy người dùng
     */
    void suspend(UUID id);

    /**
     * Kích hoạt lại tài khoản người dùng theo ID.
     *
     * <p>Khôi phục quyền truy cập cho người dùng đã bị vô hiệu hóa trước đó.</p>
     *
     * @param id id của người dùng cần mở khóa tài khoản
     * @throws BadRequestException nếu không tìm thấy người dùng
     */
    void reactivate(UUID id);
}
