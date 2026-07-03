package com.yoedu.job_board_platform.controllers;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.AuthApi;
import com.yoedu.job_board_platform.dtos.auth.*;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.mappers.AuthMapper;
import com.yoedu.job_board_platform.mappers.UserMapper;
import com.yoedu.job_board_platform.models.CookieName;
import com.yoedu.job_board_platform.security.AuthorizationConstants;
import com.yoedu.job_board_platform.services.AuthService;
import com.yoedu.job_board_platform.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.BASE + "/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {
	private final AuthService authService;
	private final AuthMapper authMapper;
	private final UserMapper userMapper;
	private final CookieUtil cookieUtil;

	@Value("${app.jwt.expiration-millis}")
	private long jwtExpirationInMs;

	@Value("${app.refresh-token.expiration-days}")
	private long refreshTokenExpirationDays;

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletResponse response) {
		AuthResult result = authService.authenticate(request.email(), request.password());
		addCookie(response, result.accessToken(), result.refreshToken());
		return ResponseEntity.ok(authMapper.toAuthResponse(result));
	}

	@PostMapping("/register/candidate")
	public ResponseEntity<Void> registerCandidate(@Valid @RequestBody CandidateRegisterRequest request) {
		authService.registerCandidate(request);
		return ResponseEntity.status(201).build();
	}

	@PostMapping("/register/company")
	public ResponseEntity<Void> registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
		authService.registerCompany(request);
		return ResponseEntity.status(201).build();
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = cookieUtil.extract(request, CookieName.REFRESH_TOKEN);
		if (refreshToken == null) {
			clearCookie(response);
			return ResponseEntity.status(401).build();
		}

		try {
			AuthResult result = authService.refreshToken(refreshToken);
			addCookie(response, result.accessToken(), result.refreshToken());
			return ResponseEntity.ok(authMapper.toAuthResponse(result));
		} catch (Exception e) {
			clearCookie(response);
			throw e;
		}
	}

	@GetMapping("/me")
	@PreAuthorize(AuthorizationConstants.AUTHENTICATED)
	public ResponseEntity<UserResponse> me() {
		return ResponseEntity.ok(userMapper.toResponse(authService.getCurrentUser()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
		String refreshToken = cookieUtil.extract(req, CookieName.REFRESH_TOKEN);
		if (refreshToken != null) {
			authService.logout(refreshToken);
		}

		clearCookie(res);

		return ResponseEntity.ok().build();
	}

	private void addCookie(HttpServletResponse response, String accessToken, String refreshToken) {
		cookieUtil.add(response, CookieName.ACCESS_TOKEN, accessToken, (int) (jwtExpirationInMs / 1000));
		cookieUtil.add(response, CookieName.REFRESH_TOKEN, refreshToken, (int) (refreshTokenExpirationDays * 86400));
	}

	private void clearCookie(HttpServletResponse response) {
		cookieUtil.clear(response, CookieName.ACCESS_TOKEN);
		cookieUtil.clear(response, CookieName.REFRESH_TOKEN);
	}
}
