package com.yoedu.job_board_platform.services.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UpdateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.mappers.UserMapper;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.ProfileService;
import com.yoedu.job_board_platform.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Triển khai UserService. Xử lý tạo và cập nhật thông tin người dùng,
 * đồng bộ với Profile tương ứng.
 */
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;

    /**
     * Tạo mới một người dùng dựa trên thông tin từ CreateUserRequest.
     * 
     * @param request thông tin người dùng cần tạo
     * @return UserResponse thông tin người dùng đã được tạo
     * @throws ConflictException nếu email đã tồn tại
     */
    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new ConflictException("Email " + request.email() + " đã tồn tại");
        }
        User user = userMapper.toEntity(request);
        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        Profile profile = profileService.createProfile(user, request.fullName(), request.phone(), request.avatarUrl());
        user.setProfile(profile);
        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }

    /**
     * Cập nhật thông tin người dùng dựa trên id và thông tin từ UpdateUserRequest.
     * 
     * @param id      id của người dùng cần cập nhật
     * @param request thông tin cập nhật
     * @return UserResponse thông tin người dùng đã được cập nhật
     * @throws BadRequestException nếu email đã tồn tại, không tìm thấy người dùng
     *                             hoặc có lỗi trong quá trình cập nhật người dùng
     */
    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        Optional<User> checkEmail = userRepository.findByEmail(request.email());
        if (checkEmail.isPresent() && !checkEmail.get().getId().equals(id)) {
            throw new BadRequestException("Email " + request.email() + " đã được sử dụng");
        }
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new BadRequestException("Không tìm thấy người dùng với id: " + id);
        }
        User user = existingUser.get();
        userMapper.updateEntity(request, user);
        if (request.password() != null && !request.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user = userRepository.save(user);

        if (request.fullName() != null || request.phone() != null || request.avatarUrl() != null) {
            Profile profile = profileRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy hồ sơ với id: " + id));
            if (request.fullName() != null)
                profile.setFullName(request.fullName());
            if (request.phone() != null)
                profile.setPhone(request.phone());
            if (request.avatarUrl() != null)
                profile.setAvatarUrl(request.avatarUrl());
            profileRepository.save(profile);
        }

        return userMapper.toResponse(user);
    }

}
