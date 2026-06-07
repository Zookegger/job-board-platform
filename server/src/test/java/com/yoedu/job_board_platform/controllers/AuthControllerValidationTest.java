package com.yoedu.job_board_platform.controllers;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

/**
 * Validation-layer integration tests for AuthController.
 *
 * All @Nested classes intentionally omit @SpringBootTest — they share the
 * single ApplicationContext started by the outer class, so the suite boots
 * once regardless of which runner or IDE button is used.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResultActions performPost(String url, Map<String, Object> body) throws Exception {
        return mockMvc.perform(
                post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));
    }

    private User savedUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("secret"))
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build());
    }

    /** Asserts the standard validation-failure envelope. */
    private static org.springframework.test.web.servlet.ResultMatcher validationFailed() {
        return jsonPath("$.message").value("Validation failed");
    }

    // =========================================================================
    // Login
    // =========================================================================

    @Nested
    class LoginValidation {

        @Test
        void blankEmail_returns400() throws Exception {
            performPost("/api/auth/login", Map.of("email", "", "password", "secret123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.email").value("Email không được để trống"));
        }

        @Test
        void invalidEmail_returns400() throws Exception {
            performPost("/api/auth/login", Map.of("email", "notanemail", "password", "secret123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.email").value("Email không hợp lệ"));
        }

        @Test
        void blankPassword_returns400() throws Exception {
            performPost("/api/auth/login", Map.of("email", "a@b.com", "password", ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.password").exists());
        }

        @Test
        void shortPassword_returns400() throws Exception {
            performPost("/api/auth/login", Map.of("email", "a@b.com", "password", "12345"))
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.password").value("Mật khẩu phải có ít nhất 6 ký tự"));
        }

        @Test
        void emptyBody_returns400WithBothErrors() throws Exception {
            performPost("/api/auth/login", Map.of())
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.email").value("Email không được để trống"))
                    .andExpect(jsonPath("$.errors.password").value("Mật khẩu không được để trống"));
        }
    }

    // =========================================================================
    // Candidate registration
    // =========================================================================

    @Nested
    class CandidateRegistrationValidation {

        private Map<String, Object> validPayload() {
            return new java.util.HashMap<>(Map.of(
                    "email", "a@b.com",
                    "fullName", "Nguyễn Văn A",
                    "password", "password123",
                    "confirmPassword", "password123"));
        }

        @Test
        void duplicateEmail_returns409() throws Exception {
            userRepository.save(User.builder()
                    .email("dup@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(UserRole.CANDIDATE)
                    .isActive(true)
                    .build());

            var payload = validPayload();
            payload.put("email", "dup@example.com");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message")
                            .value("Email dup@example.com đã tồn tại trong hệ thống"));
        }

        @Test
        void blankEmail_returns400() throws Exception {
            var payload = validPayload();
            payload.put("email", "");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.email").value("Email không được để trống"));
        }

        @Test
        void invalidEmail_returns400() throws Exception {
            var payload = validPayload();
            payload.put("email", "bad");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.email").value("Email sai định dạng"));
        }

        @Test
        void blankFullName_returns400() throws Exception {
            var payload = validPayload();
            payload.put("fullName", "");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.fullName").value("Họ tên không được để trống"));
        }

        @Test
        void blankPassword_returns400() throws Exception {
            var payload = validPayload();
            payload.put("password", "");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.password").exists());
        }

        @Test
        void shortPassword_returns400() throws Exception {
            var payload = validPayload();
            payload.put("password", "1234567");
            payload.put("confirmPassword", "1234567");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.password").value("Mật khẩu phải có ít nhất 8 ký tự"));
        }

        @Test
        void blankConfirmPassword_returns400() throws Exception {
            var payload = validPayload();
            payload.put("confirmPassword", "");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.confirmPassword").value("Yêu cầu xác nhận mật khẩu"));
        }

        @Test
        void mismatchedPasswords_returns400() throws Exception {
            var payload = validPayload();
            payload.put("password", "password123");
            payload.put("confirmPassword", "different456");

            performPost("/api/auth/register/candidate", payload)
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // Company registration
    // =========================================================================

    @Nested
    class CompanyRegistrationValidation {

        private Map<String, Object> validPayload() {
            return new java.util.HashMap<>(Map.of(
                    "companyName", "Test Corp",
                    "taxCode", "0123456789",
                    "address", "123 Street",
                    "companyPhone", "0900000000",
                    "email", "valid@company.com",
                    "fullName", "HR Manager",
                    "userEmail", "hr@company.com",
                    "userPhone", "0900000001",
                    "password", "password123",
                    "confirmPassword", "password123"));
        }

        @Test
        void companyNameTooLong_returns400() throws Exception {
            var payload = validPayload();
            payload.put("companyName", "a".repeat(101));

            performPost("/api/auth/register/company", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.companyName")
                            .value("Tên công ty không được quá 100 ký tự"));
        }

        @Test
        void taxCodeTooLong_returns400() throws Exception {
            var payload = validPayload();
            payload.put("taxCode", "a".repeat(21));

            performPost("/api/auth/register/company", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.taxCode")
                            .value("Mã số thuế không được quá 20 ký tự"));
        }

        @Test
        void companyPhoneTooLong_returns400() throws Exception {
            var payload = validPayload();
            payload.put("companyPhone", "a".repeat(16));

            performPost("/api/auth/register/company", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.companyPhone")
                            .value("Số điện thoại không được quá 15 ký tự"));
        }

        @Test
        void invalidCompanyEmail_returns400() throws Exception {
            var payload = validPayload();
            payload.put("email", "bad");

            performPost("/api/auth/register/company", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.email").value("Email sai định dạng"));
        }

        @Test
        void invalidUserEmail_returns400() throws Exception {
            var payload = validPayload();
            payload.put("userEmail", "bad");

            performPost("/api/auth/register/company", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.userEmail").value("Email sai định dạng"));
        }
    }

    // =========================================================================
    // Refresh token
    // =========================================================================

    @Nested
    class RefreshTokenValidation {

        @Test
        void noCookie_returns401() throws Exception {
            mockMvc.perform(post("/api/auth/refresh-token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void expiredToken_returns400() throws Exception {
            User user = savedUser("expired@example.com");

            RefreshToken expired = RefreshToken.builder()
                    .userId(user.getId())
                    .tokenString(UUID.randomUUID().toString())
                    .expiresAt(OffsetDateTime.now().minusDays(1))
                    .isRevoked(false)
                    .build();
            refreshTokenRepository.save(expired);

            mockMvc.perform(post("/api/auth/refresh-token")
                            .cookie(new Cookie("refreshToken", expired.getTokenString())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Refresh token đã hết hạn"));
        }

        @Test
        void revokedToken_returns400() throws Exception {
            User user = savedUser("revoked@example.com");

            RefreshToken revoked = RefreshToken.builder()
                    .userId(user.getId())
                    .tokenString(UUID.randomUUID().toString())
                    .expiresAt(OffsetDateTime.now().plusDays(7))
                    .isRevoked(true)
                    .build();
            refreshTokenRepository.save(revoked);

            mockMvc.perform(post("/api/auth/refresh-token")
                            .cookie(new Cookie("refreshToken", revoked.getTokenString())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void unknownToken_returns400OrUnauthorized() throws Exception {
            mockMvc.perform(post("/api/auth/refresh-token")
                            .cookie(new Cookie("refreshToken", UUID.randomUUID().toString())))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status != 400 && status != 401) {
                            throw new AssertionError("Expected 400 or 401 but got " + status);
                        }
                    });
        }
    }

    // =========================================================================
    // Logout
    // =========================================================================

    @Nested
    class LogoutValidation {

        @Test
        void logout_returns200() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());
        }
    }
}