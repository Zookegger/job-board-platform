package com.yoedu.job_board_platform.services.impl;

import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.mappers.CandidateMapper;
import com.yoedu.job_board_platform.mappers.CompanyMapper;
import com.yoedu.job_board_platform.models.CandidateDetail;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.security.JwtService;
import com.yoedu.job_board_platform.utils.SecurityUtil;
import com.yoedu.job_board_platform.services.AuthService;
import com.yoedu.job_board_platform.services.ProfileService;
import com.yoedu.job_board_platform.services.RefreshTokenService;
import com.yoedu.job_board_platform.utils.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Triển khai AuthService. Xử lý xác thực, đăng ký và quản lý token.
 * Sử dụng PasswordEncoder để mã hóa mật khẩu và JwtService để tạo JWT.
 */
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CandidateMapper candidateMapper;
    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;
    private final CandidateDetailRepository candidateDetailRepository;
    private final CompanyEmployerDetailRepository employerRepository;
    private final ProfileService profileService;
    private final SecurityUtil securityUtil;

    @Override
    public AuthResult authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UserDetails userDetails = toUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResult(accessToken, refreshToken.getTokenString(), user.getRole(),
                jwtService.getJwtExpirationInMs());
    }

    @Override
    public AuthResult refreshToken(String tokenString) {
        RefreshToken stored = refreshTokenService.validateRefreshToken(tokenString);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        UserDetails userDetails = toUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);
        refreshTokenService.revokeRefreshToken(tokenString);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResult(accessToken, newRefreshToken.getTokenString(), user.getRole(),
                jwtService.getJwtExpirationInMs());
    }

    @Override
    @Transactional
    public void registerCandidate(CandidateRegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email " + request.email() + " đã tồn tại trong hệ thống");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không trùng");
        }

        User user = candidateMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        Profile profile = profileService.createProfile(user, request.fullName(), "", null);
        user.setProfile(profile);

        CandidateDetail detail = CandidateDetail.builder().profileId(profile.getId()).build();
        candidateDetailRepository.save(detail);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void registerCompany(CompanyRegisterRequest request) {
        // 1. Kiểm tra email công ty không trùng
        if (companyRepository.existsByCompanyName(request.companyName()))
            throw new ConflictException("Tên công ty này đã được đăng ký. Nếu đây là công ty của bạn, vui lòng đăng nhập để tiếp tục.");

        // 2. Validate confirmPassword == password
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không trùng");
        }

        // 3. Tạo User: role = EMPLOYER, isActive = true, email, password (encoded)
        User user = companyMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        // 4. Tạo Profile: id = userId, fullName, phone từ request
        Profile profile = profileService.createProfile(user, request.fullName(), request.userPhone(), null);
        user.setProfile(profile);

        // 5. Tạo Company: status = PENDING, isApproved = false, slug tự sinh từ companyName
        Company company = companyMapper.toEntity(request);
        company.setSlug(
                StringUtils.slugifyUnique(request.companyName(), (slug) -> companyRepository.existsBySlug(slug)));
        companyRepository.save(company);

        // 7. Tạo CompanyEmployerDetail: link Profile → Company, roleInCompany = "HR
        // Representative"
        CompanyEmployerDetail detail = CompanyEmployerDetail.builder()
                .profileId(profile.getId())
                .profile(profile)
                .company(company)
                .roleInCompany("HR")
                .build();
        employerRepository.save(detail);

        // TODO: 8. Tạo Notification cho tất cả ADMIN: type = COMPANY_PENDING_REVIEW,
        // message
        // = "Công ty {companyName} đã đăng ký và đang chờ duyệt", entityId = companyId
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    public UUID extractUserId(String accessToken) {
        String email = jwtService.extractUsername(accessToken);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"))
                .getId();
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }

    @Override
    public User getCurrentUser() {
        return securityUtil.getCurrentUser();
    }

}
