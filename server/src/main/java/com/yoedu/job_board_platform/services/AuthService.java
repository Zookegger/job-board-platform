package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.models.User;

/**
 * Service xác thực và đăng ký người dùng.
 * Xử lý đăng nhập, refresh token, đăng ký ứng viên/nhà tuyển dụng,
 * đăng xuất và trích xuất thông tin người dùng từ token.
 */
public interface AuthService {

    /**
     * Xác thực người dùng với email và mật khẩu.
     * Nếu thành công, trả về access token và refresh token.
     *
     * @param email    email của người dùng
     * @param password mật khẩu của người dùng
     * @return AuthResult chứa access token, refresh token và thông tin xác thực
     */
    AuthResult authenticate(String email, String password);

    /**
     * Làm mới access token bằng refresh token.
     * Thu hồi refresh token cũ và tạo refresh token mới.
     *
     * @param tokenString chuỗi refresh token hợp lệ
     * @return AuthResult chứa access token mới và refresh token mới
     */
    AuthResult refreshToken(String tokenString);

    /**
     * Đăng ký tài khoản ứng viên mới.
     * Tạo User với role CANDIDATE, Profile và CandidateDetail.
     *
     * @param request thông tin đăng ký của ứng viên
     */
    void registerCandidate(CandidateRegisterRequest request);

    /**
     * Đăng ký tài khoản nhà tuyển dụng và công ty mới.
     * Tạo User với role EMPLOYER, Profile, Company và CompanyEmployerDetail.
     * Công ty được tạo với trạng thái PENDING chờ admin duyệt.
     *
     * @param request thông tin đăng ký của nhà tuyển dụng
     */
    void registerCompany(CompanyRegisterRequest request);

    /**
     * Đăng xuất người dùng bằng cách thu hồi refresh token.
     *
     * @param refreshToken chuỗi refresh token cần thu hồi
     */
    void logout(String refreshToken);

    /**
     * Trích xuất ID của người dùng từ access token.
     *
     * @param accessToken access token của người dùng
     * @return UUID ID của người dùng
     */
    UUID extractUserId(String accessToken);

    /**
     * Lấy thông tin người dùng hiện tại từ context bảo mật.
     *
     * @return User đối tượng người dùng hiện tại
     */
    User getCurrentUser();
}
