package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.RefreshTokenService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void TC_03_loginReturnsAccessAndRefreshTokens() throws Exception {
        // Arrange: create user
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("P@ssw0rd"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build();
        userRepository.save(user);

        // Act: perform login
        var loginPayload = objectMapper.writeValueAsString(
                java.util.Map.of("email", "test@example.com", "password", "P@ssw0rd"));

        MvcResult res = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String setCookie = res.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("accessToken=");
        assertThat(res.getResponse().getHeaders("Set-Cookie"))
                .anyMatch(header -> header.contains("refreshToken="));

        String body = res.getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        assertThat(json.get("accessToken").asText()).isNotBlank();
        assertThat(json.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void TC_05_refreshWithValidTokenIssuesNewAccessToken() throws Exception {
        // Arrange: create user and refresh token in DB
        User user = User.builder()
                .email("refresh@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .build();
        userRepository.save(user);

        RefreshToken rt = RefreshToken.builder()
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(false)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(rt);

        // Act: call refresh endpoint with cookie
        Cookie cookie = new Cookie("refreshToken", rt.getTokenString());
        MvcResult res = mockMvc.perform(post("/api/auth/refresh-token").cookie(cookie))
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
                .email("wrong@example.com")
                .password(passwordEncoder.encode("CorrectPassword"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(user);

        // Act: attempt login with wrong password
        var loginPayload = objectMapper.writeValueAsString(
                java.util.Map.of("email", "wrong@example.com", "password", "WrongPassword"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void TC_06_refreshFailsWithRevokedToken() throws Exception {
        // Arrange: create user and revoked refresh token
        User user = User.builder()
                .email("revoked@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build();
        userRepository.save(user);

        RefreshToken revokedToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(true)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(revokedToken);

        // Act: call refresh endpoint with revoked token
        Cookie cookie = new Cookie("refreshToken", revokedToken.getTokenString());
        mockMvc.perform(post("/api/auth/refresh-token").cookie(cookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC_07_logout_revokesTokenInDatabase() throws Exception {
        User user = User.builder()
                .email("logout-revoke@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build();
        userRepository.save(user);

        RefreshToken token = RefreshToken.builder()
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(false)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        Cookie cookie = new Cookie("refreshToken", token.getTokenString());
        mockMvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isOk());

        RefreshToken saved = refreshTokenRepository.findById(token.getId()).orElseThrow();
        assertThat(saved.isRevoked()).isTrue();
    }

    @Test
    void TC_08_logout_clearsCookies() throws Exception {
        User user = User.builder()
                .email("logout-cookies@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build();
        userRepository.save(user);

        RefreshToken token = RefreshToken.builder()
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(false)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        Cookie cookie = new Cookie("refreshToken", token.getTokenString());
        MvcResult res = mockMvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isOk())
                .andReturn();

        var setCookieHeaders = res.getResponse().getHeaders("Set-Cookie");
        assertThat(setCookieHeaders).anyMatch(h -> h.contains("accessToken=") && h.contains("Max-Age=0"));
        assertThat(setCookieHeaders).anyMatch(h -> h.contains("refreshToken=") && h.contains("Max-Age=0"));
    }

    @Test
    void TC_09_logout_withoutCookie_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    void TC_10_logout_thenRefreshFails() throws Exception {
        User user = User.builder()
                .email("logout-then-refresh@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build();
        userRepository.save(user);

        RefreshToken token = RefreshToken.builder()
                .userId(user.getId())
                .tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .isRevoked(false)
                .createdAt(OffsetDateTime.now())
                .build();
        refreshTokenRepository.save(token);

        Cookie cookie = new Cookie("refreshToken", token.getTokenString());
        mockMvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh-token").cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token đã bị thu hồi"));
    }

    @Test
    void TC_11_login_logout_login_secondLoginWorks() throws Exception {
        userRepository.save(User.builder()
                .email("login-logout-login@example.com")
                .password(passwordEncoder.encode("P@ssw0rd"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build());

        var loginPayload = objectMapper.writeValueAsString(
                java.util.Map.of("email", "login-logout-login@example.com", "password", "P@ssw0rd"));

        MvcResult firstLogin = mockMvc
                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String firstRefreshToken = objectMapper.readTree(firstLogin.getResponse().getContentAsString())
                .get("refreshToken").asText();

        Cookie logoutCookie = new Cookie("refreshToken", firstRefreshToken);
        mockMvc.perform(post("/api/auth/logout").cookie(logoutCookie))
                .andExpect(status().isOk());

        MvcResult secondLogin = mockMvc
                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String secondRefreshToken = objectMapper.readTree(secondLogin.getResponse().getContentAsString())
                .get("refreshToken").asText();
        assertThat(secondRefreshToken).isNotBlank();
        assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken);
    }

    @Test
    void TC_12_revokeRefreshToken_withNonExistentToken_throws() {
        assertThrows(BadRequestException.class,
                () -> refreshTokenService.revokeRefreshToken("nonexistent-token"));
    }

    @Test
    void TC_13_revokeAllUserTokens_revokesAllTokens() throws Exception {
        User user = User.builder()
                .email("revoke-all@example.com")
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build();
        userRepository.save(user);

        RefreshToken t1 = RefreshToken.builder()
                .userId(user.getId()).tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7)).isRevoked(false)
                .createdAt(OffsetDateTime.now()).build();
        RefreshToken t2 = RefreshToken.builder()
                .userId(user.getId()).tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7)).isRevoked(false)
                .createdAt(OffsetDateTime.now()).build();
        RefreshToken t3 = RefreshToken.builder()
                .userId(user.getId()).tokenString(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusDays(7)).isRevoked(false)
                .createdAt(OffsetDateTime.now()).build();
        refreshTokenRepository.saveAll(List.of(t1, t2, t3));

        refreshTokenService.revokeAllUserTokens(user.getId());

        assertThat(refreshTokenRepository.findById(t1.getId()).orElseThrow().isRevoked()).isTrue();
        assertThat(refreshTokenRepository.findById(t2.getId()).orElseThrow().isRevoked()).isTrue();
        assertThat(refreshTokenRepository.findById(t3.getId()).orElseThrow().isRevoked()).isTrue();
    }
}
