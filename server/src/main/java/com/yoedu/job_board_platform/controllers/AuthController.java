package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.dtos.auth.AuthResponse;
import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.LoginRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;
import com.yoedu.job_board_platform.mappers.AuthMapper;
import com.yoedu.job_board_platform.mappers.UserMapper;
import com.yoedu.job_board_platform.models.CookieName;
import com.yoedu.job_board_platform.services.AuthService;
import com.yoedu.job_board_platform.services.UserService;
import com.yoedu.job_board_platform.utils.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + "/auth")
@RequiredArgsConstructor
@Tag(name = "Auth & Tài khoản", description = "Đăng ký, đăng nhập, làm mới token, lấy thông tin user hiện tại. Không yêu cầu xác thực cho login/register.")
public class AuthController {
	private final AuthService authService;
	private final AuthMapper authMapper;
	private final UserMapper userMapper;
	private final CookieUtil cookieUtil;
	private final UserService userService;

	@PostMapping("/login")
	@Operation(summary = "Đăng nhập", description = """
			Xác thực email và mật khẩu, trả về JWT access token + refresh token.
			Token được set trong HttpOnly cookie và cũng trả về trong response body.
			Gọi endpoint này trước khi sử dụng các API yêu cầu xác thực.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Đăng nhập thành công — trả về access token, refresh token, token type, thời gian hết hạn", content = @Content(mediaType = "application/json", examples = @ExampleObject("""
					{
					    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
					    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
					    "tokenType": "Bearer",
					    "expiresIn": 3600000
					}
					"""))),
			@ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu", content = @Content)
	})
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletResponse response) {
		AuthResult result = authService.authenticate(request.email(), request.password());
		addCookie(response, result.accessToken(), result.refreshToken());
		return ResponseEntity.ok(authMapper.toAuthResponse(result));
	}

	@PostMapping("/register/candidate")
	@Operation(summary = "Đăng ký tài khoản ứng viên", description = """
			Tạo tài khoản mới với vai trò CANDIDATE.
			Yêu cầu email chưa tồn tại trong hệ thống, mật khẩu ít nhất 8 ký tự,
			và confirmPassword phải khớp với password.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Đăng ký thành công — tài khoản ứng viên đã được tạo", content = @Content),
			@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (email sai format, thiếu trường, mật khẩu không khớp)", content = @Content),
			@ApiResponse(responseCode = "409", description = "Email đã tồn tại trong hệ thống", content = @Content)
	})
	public ResponseEntity<Void> registerCandidate(@Valid @RequestBody CandidateRegisterRequest request) {
		authService.registerCandidate(request);
		return ResponseEntity.status(201).build();
	}

	@PostMapping("/register/company")
	@Operation(summary = "Đăng ký tài khoản nhà tuyển dụng", description = """
			Tạo tài khoản mới với vai trò EMPLOYER, đồng thời tạo một công ty mới
			với trạng thái PENDING chờ admin phê duyệt.
			Yêu cầu email công ty chưa tồn tại trong hệ thống.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Đăng ký thành công — tài khoản nhà tuyển dụng và công ty đã được tạo", content = @Content),
			@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc mật khẩu xác nhận không trùng", content = @Content),
			@ApiResponse(responseCode = "409", description = "Email công ty đã tồn tại", content = @Content)
	})
	public ResponseEntity<Void> registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
		authService.registerCompany(request);
		return ResponseEntity.status(201).build();
	}

	@PostMapping("/refresh-token")
	@Operation(summary = "Làm mới access token", description = """
			Dùng refresh token (từ cookie) để lấy access token mới.
			Hữu ích khi access token hết hạn — tránh cho người dùng phải đăng nhập lại.
			Refresh token cũng được refresh (luân chuyển).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Access token mới đã được cấp — trả về token mới và set cookie", content = @Content(mediaType = "application/json", examples = @ExampleObject("""
					{
					    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
					    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
					    "tokenType": "Bearer",
					    "expiresIn": 3600000
					}
					"""))),
			@ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ hoặc đã hết hạn", content = @Content)
	})
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
	@Operation(summary = "Thông tin user hiện tại", description = """
			Trả về thông tin cá nhân của user đang đăng nhập (dựa trên JWT token).
			Bao gồm id, email, vai trò, trạng thái hoạt động và họ tên.
			Yêu cầu token hợp lệ (Bearer token hoặc cookie).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Thông tin user hiện tại", content = @Content(mediaType = "application/json", examples = @ExampleObject("""
					{
					    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
					    "email": "user@example.com",
					    "role": "CANDIDATE",
					    "isActive": true,
					    "fullName": "Nguyễn Văn A"
					}
					"""))),
			@ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc đã hết hạn", content = @Content)
	})
	public ResponseEntity<UserResponse> me() {
		return ResponseEntity.ok(userMapper.toResponse(userService.getCurrentUser()));
	}

	@PostMapping("/logout")
	@Operation(summary = "Đăng xuất", description = """
			Thu hồi refresh token hiện tại và xóa cookie đăng nhập.
			Sau khi logout, client cần xóa token khỏi local storage (nếu có).
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Đăng xuất thành công — token đã bị thu hồi", content = @Content),
	})
	public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
		String refreshToken = cookieUtil.extract(req, CookieName.REFRESH_TOKEN);
		if (refreshToken != null) {
			authService.logout(refreshToken);
		}

		clearCookie(res);

		return ResponseEntity.ok().build();
	}

	private void addCookie(HttpServletResponse response, String accessToken, String refreshToken) {
		cookieUtil.add(response, CookieName.ACCESS_TOKEN, accessToken);
		cookieUtil.add(response, CookieName.REFRESH_TOKEN, refreshToken);
	}

	private void clearCookie(HttpServletResponse response) {
		cookieUtil.clear(response, CookieName.ACCESS_TOKEN);
		cookieUtil.clear(response, CookieName.REFRESH_TOKEN);
	}
}
