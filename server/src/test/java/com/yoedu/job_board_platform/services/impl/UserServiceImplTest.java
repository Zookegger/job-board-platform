package com.yoedu.job_board_platform.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.dtos.user.CreateUserRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.mappers.UserMapper;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.ProfileService;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void TC_01_createSuccessWithNewEmail() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("newuser@example.com", "Password123", UserRole.CANDIDATE,
                "New User", "0123456789", "");
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("newuser@example.com")
                .password("encodedPassword")
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();
        UserResponse expectedResponse = new UserResponse(savedUser.getId(), savedUser.getEmail(),
                savedUser.getRole().name(), savedUser.isActive(), "New User");

        User unsavedUser = User.builder()
                .email("newuser@example.com")
                .password("Password123")
                .role(UserRole.CANDIDATE)
                .build();

        when(userMapper.toEntity(request)).thenReturn(unsavedUser);
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        UserResponse result = userService.create(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("newuser@example.com");
        assertThat(result.id()).isEqualTo(savedUser.getId());
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, times(2)).save(any(User.class));
        verify(profileService, times(1)).createProfile(any(User.class), any(), any(), any());
    }

    @Test
    void TC_02_createFailsWithDuplicateEmail() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("existing@example.com", "Password123", UserRole.CANDIDATE,
                "Existing User", "0123456789", "");
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("existing@example.com")
                .password("encodedPassword")
                .role(UserRole.CANDIDATE)
                .build();

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email existing@example.com đã tồn tại");

        verify(userRepository, times(1)).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}
