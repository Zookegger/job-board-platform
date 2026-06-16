package com.yoedu.job_board_platform.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.mappers.ProfileMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.services.ProfileService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final CompanyEmployerDetailRepository companyEmployerDetailRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtil securityUtil;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public CandidateProfileResponse getCurrentCandidateProfile() {
        User user = securityUtil.getCurrentUser();
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new ForbiddenException("Chỉ ứng viên mới có thể xem hồ sơ ứng viên");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));
        return profileMapper.toCandidateResponse(profile);
    }

    @Override
    public EmployerProfileResponse getCurrentEmployerProfile() {
        User user = securityUtil.getCurrentUser();

        if (user.getRole() != UserRole.EMPLOYER) {
            throw new ForbiddenException("Chỉ nhà tuyển dụng mới có thể xem hồ sơ nhà tuyển dụng");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));
        return profileMapper.toEmployerResponse(profile);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateCurrentCandidateProfile(CandidateProfileRequest request) {
        User user = securityUtil.getCurrentUser();

        if (user.getRole() != UserRole.CANDIDATE) {
            throw new ForbiddenException("Chỉ ứng viên mới có thể cập nhật hồ sơ ứng viên");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        profileMapper.updateCandidateEntity(request, profile);
        profileRepository.save(profile);

        return profileMapper.toCandidateResponse(profile);
    }

    @Override
    @Transactional
    public EmployerProfileResponse updateCurrentEmployerProfile(EmployerProfileRequest request) {
        User user = securityUtil.getCurrentUser();

        if (user.getRole() != UserRole.EMPLOYER) {
            throw new ForbiddenException("Chỉ nhà tuyển dụng mới có thể cập nhật hồ sơ nhà tuyển dụng");
        }

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ"));

        profileMapper.updateEmployerEntity(request, profile);
        profileRepository.save(profile);

        CompanyEmployerDetail employerDetail = companyEmployerDetailRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin nhà tuyển dụng"));

        if (request.roleInCompany() != null) {
            employerDetail.setRoleInCompany(request.roleInCompany());
            companyEmployerDetailRepository.save(employerDetail);
        }

        Company company = employerDetail.getCompany();
        boolean companyChanged = false;
        if (request.companyName() != null) {
            company.setCompanyName(request.companyName());
            companyChanged = true;
        }
        if (request.address() != null) {
            company.setAddress(request.address());
            companyChanged = true;
        }
        if (request.description() != null) {
            company.setDescription(request.description());
            companyChanged = true;
        }
        if (request.website() != null) {
            company.setWebsite(request.website());
            companyChanged = true;
        }
        if (request.logoUrl() != null) {
            company.setLogoUrl(request.logoUrl());
            companyChanged = true;
        }
        if (request.companyEmail() != null) {
            company.setEmail(request.companyEmail());
            companyChanged = true;
        }
        if (request.companyPhone() != null) {
            company.setPhone(request.companyPhone());
            companyChanged = true;
        }
        if (request.taxCode() != null) {
            company.setTaxCode(request.taxCode());
            companyChanged = true;
        }
        if (companyChanged) {
            companyRepository.save(company);
        }

        return profileMapper.toEmployerResponse(profile);
    }

    @Override
    @Transactional
    public Profile createProfile(User user, String fullName, String phone, String avatarUrl) {
        Profile profile = Profile.builder()
                .user(user)
                .fullName(fullName)
                .phone(phone != null ? phone : "")
                .avatarUrl(avatarUrl)
                .build();
        return profileRepository.save(profile);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        Profile userProfile = securityUtil.getCurrentUser().getProfile();

        if (file.isEmpty())
            throw new BadRequestException("Không có file");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new BadRequestException("Chỉ hỗ trợ định dạng hình ảnh");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new BadRequestException("File quá lớn. Dung lượng tối đa: 5MB");

        var avatarDir = Paths.get(uploadDir, "avatars");
        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            log.error("Lỗi lưu ảnh avatar", e);
            throw new RuntimeException("Lỗi tạo thư mục ảnh avatar", e);
        }

        var ext = contentType.substring(contentType.lastIndexOf('/') + 1); // "png", "jpeg"
        var fileName = UUID.randomUUID() + "." + ext;
        var targetPath = avatarDir.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Lỗi lưu ảnh avatar", e);
            throw new RuntimeException("Lỗi lưu ảnh avatar", e);
        }

        if (userProfile.getAvatarUrl() != null && !userProfile.getAvatarUrl().isBlank()) {
            try {
                var oldPath = userProfile.getAvatarUrl().startsWith("/uploads/")
                        ? Paths.get(uploadDir, userProfile.getAvatarUrl().replace("/uploads/", ""))
                        : Paths.get(userProfile.getAvatarUrl());
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                log.error("Lỗi xóa ảnh avatar cũ", e);
                throw new RuntimeException("Lỗi xóa ảnh avatar cũ", e);
            }
        }

        var avatarUrl = "/uploads/avatars/" + fileName;
        userProfile.setAvatarUrl(avatarUrl);
        profileRepository.save(userProfile);
        return avatarUrl;
    }

    @Override
    public String uploadCompanyLogo(MultipartFile file) {
        User user = securityUtil.getCurrentUser();

        if (user.getRole() != UserRole.EMPLOYER)
            throw new ForbiddenException("Chỉ nhà tuyển dụng mới có thể upload logo công ty");

        if (file.isEmpty())
            throw new BadRequestException("Không có file");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new BadRequestException("Chỉ hỗ trợ định dạng hình ảnh");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new BadRequestException("File quá lớn. Dung lượng tối đa: 5MB");

        var logoDir = Paths.get(uploadDir, "logos");
        try {
            Files.createDirectories(logoDir);
        } catch (IOException e) {
            log.error("Lỗi tạo thư mục logo", e);
            throw new RuntimeException("Lỗi tạo thư mục logo công ty", e);
        }

        var ext = contentType.substring(contentType.lastIndexOf('/') + 1);
        var fileName = UUID.randomUUID() + "." + ext;
        var targetPath = logoDir.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Lỗi lưu ảnh logo", e);
            throw new RuntimeException("Lỗi lưu logo công ty", e);
        }

        Company company = user.getProfile().getEmployerDetail().getCompany();

        if (company.getLogoUrl() != null && !company.getLogoUrl().isBlank()) {
            try {
                var oldPath = company.getLogoUrl().startsWith("/uploads/")
                        ? Paths.get(uploadDir, company.getLogoUrl().replace("/uploads/", ""))
                        : Paths.get(company.getLogoUrl());
                Files.deleteIfExists(oldPath);
            } catch (IOException e) {
                log.error("Lỗi xóa logo cũ", e);
            }
        }

        var logoUrl = "/uploads/logos/" + fileName;
        company.setLogoUrl(logoUrl);
        companyRepository.save(company);
        return logoUrl;
    }
}
