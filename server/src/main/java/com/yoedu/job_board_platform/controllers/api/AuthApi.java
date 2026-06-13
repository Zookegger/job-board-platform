package com.yoedu.job_board_platform.controllers.api;

import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.auth.AuthResponse;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.LoginRequest;
import com.yoedu.job_board_platform.dtos.user.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth & Tài khoản", description = "Đăng ký, đăng nhập, làm mới token, lấy thông tin user hiện tại. Không yêu cầu xác thực cho login/register.")
public interface AuthApi {

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
    ResponseEntity<AuthResponse> login(LoginRequest request, HttpServletResponse response);

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
    ResponseEntity<Void> registerCandidate(CandidateRegisterRequest request);

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
    ResponseEntity<Void> registerCompany(CompanyRegisterRequest request);

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
    ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response);

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
    ResponseEntity<UserResponse> me();

    @Operation(summary = "Đăng xuất", description = """
            Thu hồi refresh token hiện tại và xóa cookie đăng nhập.
            Sau khi logout, client cần xóa token khỏi local storage (nếu có).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công — token đã bị thu hồi", content = @Content),
    })
    ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res);
}
