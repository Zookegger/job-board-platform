package com.yoedu.job_board_platform.utils;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Tiện ích lấy thông tin người dùng hiện tại từ SecurityContext.
 * <p>
 * Được inject vào các service để tránh phụ thuộc vòng (circular dependency)
 * giữa {@code AuthServiceImpl} và {@code ProfileServiceImpl}.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final UserRepository userRepository;

    /**
     * Lấy thông tin người dùng hiện tại từ SecurityContext.
     *
     * @return {@link User} đã xác thực
     * @throws ResourceNotFoundException nếu không có authentication hoặc không tìm
     *                                   thấy user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại"));
    }

    /**
     * Lấy ID người dùng hiện tại từ SecurityContext.
     *
     * @return ID {@link User} đã xác thực
     * @throws ResourceNotFoundException nếu không có authentication hoặc không tìm
     *                                   thấy user
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại")).getId();

    }

    public boolean isAuthorized(UUID userId, List<UserRole> roles) {
        User user = getCurrentUser();

        if (!user.getId().equals(userId)) {
            throw new ForbiddenException("Người dùng không hợp lệ");
        }

        if (!roles.contains(user.getRole())) {
            throw new ForbiddenException("Người dùng không có quyền truy cập tài nguyên này");
        }

        return true;
    }
}
