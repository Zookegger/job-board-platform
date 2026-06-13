package com.yoedu.job_board_platform.services;

import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;

/**
 * Service quản lý hồ sơ người dùng (Profile).
 * Hỗ trợ xem/cập nhật hồ sơ cho ứng viên và nhà tuyển dụng,
 * tạo hồ sơ mới và upload avatar.
 */
public interface ProfileService {

    /**
     * Lấy hồ sơ ứng viên của người dùng hiện tại.
     *
     * @return CandidateProfileResponse thông tin hồ sơ ứng viên
     */
    CandidateProfileResponse getCurrentCandidateProfile();

    /**
     * Lấy hồ sơ nhà tuyển dụng của người dùng hiện tại.
     *
     * @return EmployerProfileResponse thông tin hồ sơ nhà tuyển dụng
     */
    EmployerProfileResponse getCurrentEmployerProfile();

    /**
     * Cập nhật hồ sơ ứng viên của người dùng hiện tại.
     *
     * @param request thông tin hồ sơ cần cập nhật
     * @return CandidateProfileResponse thông tin hồ sơ sau khi cập nhật
     */
    CandidateProfileResponse updateCurrentCandidateProfile(CandidateProfileRequest request);

    /**
     * Cập nhật hồ sơ nhà tuyển dụng của người dùng hiện tại.
     * Có thể cập nhật thông tin công ty (tên, địa chỉ, mô tả, website, logo).
     *
     * @param request thông tin hồ sơ cần cập nhật
     * @return EmployerProfileResponse thông tin hồ sơ sau khi cập nhật
     */
    EmployerProfileResponse updateCurrentEmployerProfile(EmployerProfileRequest request);

    /**
     * Tạo hồ sơ mới cho người dùng.
     *
     * @param user      người dùng cần tạo hồ sơ
     * @param fullName  họ và tên
     * @param phone     số điện thoại
     * @param avatarUrl đường dẫn ảnh đại diện
     * @return Profile hồ sơ đã được tạo
     */
    Profile createProfile(User user, String fullName, String phone, String avatarUrl);

    /**
     * Upload ảnh đại diện cho người dùng hiện tại.
     * Chấp nhận định dạng hình ảnh, kích thước tối đa 5MB.
     *
     * @param file file ảnh cần upload
     * @return String đường dẫn URL của ảnh đại diện
     */
    String uploadAvatar(MultipartFile file);
}
