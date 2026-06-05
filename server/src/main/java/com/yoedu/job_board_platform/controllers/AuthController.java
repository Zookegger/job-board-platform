package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.dtos.auth.AuthResponse;
import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.LoginRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.mappers.AuthMapper;
import com.yoedu.job_board_platform.mappers.UserMapper;
import com.yoedu.job_board_platform.services.AuthService;
import com.yoedu.job_board_platform.services.UserService;
import com.yoedu.job_board_platform.utils.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth & Tài khoản", description = "Đăng ký, đăng nhập, làm mới token")
public class AuthController {
    private final AuthService authService;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final CookieUtil cookieUtil;
    private final UserService userService;
    private static final String ACCESS_COOKIE = "accessToken";
    private static final String REFRESH_COOKIE = "refreshToken";

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập → JWT + refresh token", description = "Trả về access token và refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResult result = authService.authenticate(request.email(), request.password());

        cookieUtil.add(response, ACCESS_COOKIE, result.accessToken());
        cookieUtil.add(response, REFRESH_COOKIE, result.refreshToken());

        return ResponseEntity.ok(authMapper.toAuthResponse(result));
    }

    @PostMapping("/register/candidate")
    @Operation(summary = "Đăng ký ứng viên")
    public ResponseEntity<Void> registerCandidate(@RequestBody CandidateRegisterRequest request) {
        authService.registerCandidate(request);
        return ResponseEntity.status(201).build();
    }

    // @PostMapping("/register/company")
    // public ResponseEntity<Void> registerCompany(@RequestBody CompanyRegisterRequest request) {
    //     authService.registerCompany()

    // }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới access token", description = "Dùng refresh token để lấy access token mới")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extract(request, REFRESH_COOKIE);
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        AuthResult result = authService.refreshToken(refreshToken);

        cookieUtil.add(response, ACCESS_COOKIE, result.accessToken());

        return ResponseEntity.ok(authMapper.toAuthResponse(result));
    }

    // logout

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin user hiện tại", description = "Yêu cầu JWT token")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userMapper.toResponse(userService.getCurrentUser()));
    }
}
