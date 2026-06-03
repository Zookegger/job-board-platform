package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.utility.TestcontainersConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@Import(TestcontainersConfiguration.class)
public class AuthControllerIntegrationTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void TC_03_loginReturnsAccessAndRefreshTokens() throws Exception {
        // Arrange: create user
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password(passwordEncoder.encode("P@ssw0rd"))
                .role(UserRole.JOB_SEEKER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(user);

        // Act: perform login
        var loginPayload = objectMapper.writeValueAsString(
                java.util.Map.of("email", "test@example.com", "password", "P@ssw0rd"));

        MvcResult res = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("accessToken=").contains("refreshToken=");

        String body = res.getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        assertThat(json.get("accessToken").asText()).isNotBlank();
        assertThat(json.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void TC_05_refreshWithValidTokenIssuesNewAccessToken() throws Exception {
        // Arrange: create user and refresh token in DB
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("refresh@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(user);

        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(false)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(rt);

        // Act: call refresh endpoint with cookie
        Cookie cookie = new Cookie("refreshToken", rt.getTokenString());
        MvcResult res = mockMvc.perform(post("/auth/refresh").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("accessToken=");

        String body = res.getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        assertThat(json.get("accessToken").asText()).isNotBlank();
    }

    @Test
    void TC_04_loginFailsWithWrongPassword() throws Exception {
        // Arrange: create user
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("wrong@example.com")
                .password(passwordEncoder.encode("CorrectPassword"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(user);

        // Act: attempt login with wrong password
        var loginPayload = objectMapper.writeValueAsString(
                java.util.Map.of("email", "wrong@example.com", "password", "WrongPassword"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_06_refreshFailsWithRevokedToken() throws Exception {
        // Arrange: create user and revoked refresh token
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("revoked@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.JOB_SEEKER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();
        userRepository.save(user);

        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(true)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(revokedToken);

        // Act: call refresh endpoint with revoked token
        Cookie cookie = new Cookie("refreshToken", revokedToken.getTokenString());
        mockMvc.perform(post("/auth/refresh").cookie(cookie))
                .andExpect(status().isUnauthorized());
    }
}
