package com.yoedu.job_board_platform.services.impl;

import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.dtos.auth.AuthResult;
import com.yoedu.job_board_platform.dtos.auth.CandidateRegisterRequest;
import com.yoedu.job_board_platform.dtos.auth.CompanyRegisterRequest;
import com.yoedu.job_board_platform.mappers.CandidateMapper;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.security.JwtService;
import com.yoedu.job_board_platform.services.AuthService;
import com.yoedu.job_board_platform.services.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CandidateMapper candidateMapper;

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

        return new AuthResult(accessToken, refreshToken.getTokenString(), user.getRole(), jwtService.getJwtExpirationInMs());
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

        return new AuthResult(accessToken, newRefreshToken.getTokenString(), user.getRole(), jwtService.getJwtExpirationInMs());
    }

    @Override
    public void registerCandidate(CandidateRegisterRequest request) {
        User user = candidateMapper.toUser(request);
        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Override
    public void registerCompany(CompanyRegisterRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
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

}
