package com.yoedu.job_board_platform.controllers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.RefreshToken;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.RefreshTokenService;

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
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthControllerValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CompanyEmployerDetailRepository companyEmployerDetailRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired RefreshTokenService refreshTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        companyEmployerDetailRepository.deleteAll();
        companyRepository.deleteAll();
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
        void userPhoneTooLong_returns400() throws Exception {
            var payload = validPayload();
            payload.put("userPhone", "a".repeat(16));

            performPost("/api/auth/register/company", payload)
                    .andExpect(status().isBadRequest())
                    .andExpect(validationFailed())
                    .andExpect(jsonPath("$.errors.userPhone")
                            .value("Số điện thoại không được quá 15 ký tự"));
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

    // =========================================================================
    // Registration flow
    // =========================================================================

    @Nested
    class RegistrationFlowTest {

        @Test
        void registerCandidate_WithValidData_Returns201AndCreatesUser() throws Exception {
            var payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "email", "candidate@example.com",
                    "fullName", "Nguyễn Văn A",
                    "password", "password123",
                    "confirmPassword", "password123"));

            mockMvc.perform(post("/api/auth/register/candidate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());

            User saved = userRepository.findByEmail("candidate@example.com").orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getRole()).isEqualTo(UserRole.CANDIDATE);
            assertThat(saved.isActive()).isTrue();

            assertThat(profileRepository.findById(saved.getId())).isPresent()
                    .hasValueSatisfying(p -> {
                        assertThat(p.getFullName()).isEqualTo("Nguyễn Văn A");
                        assertThat(p.getPhone()).isEqualTo("");
                    });
        }

        @Test
        void registerCompany_WithValidData_Returns201AndCreatesAllEntities() throws Exception {
            var payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "companyName", "Yoedu Technology Corporation",
                    "taxCode", "0123456789",
                    "address", "123 Nguyễn Huệ, Quận 1, TP.HCM",
                    "fullName", "Nguyễn Văn A",
                    "userEmail", "recruiter@yoedu.com",
                    "userPhone", "0987654321",
                    "password", "password123",
                    "confirmPassword", "password123"));

            mockMvc.perform(post("/api/auth/register/company")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());

            User user = userRepository.findByEmail("recruiter@yoedu.com").orElse(null);
            assertThat(user).isNotNull();
            assertThat(user.getRole()).isEqualTo(UserRole.EMPLOYER);
            assertThat(user.isActive()).isTrue();

            assertThat(profileRepository.findById(user.getId())).isPresent()
                    .hasValueSatisfying(p -> {
                        assertThat(p.getFullName()).isEqualTo("Nguyễn Văn A");
                        assertThat(p.getPhone()).isEqualTo("0987654321");
                    });

            var companies = companyRepository.findAll();
            assertThat(companies).hasSize(1);
            Company company = companies.get(0);
            assertThat(company.getCompanyName()).isEqualTo("Yoedu Technology Corporation");
            assertThat(company.getAddress()).isEqualTo("123 Nguyễn Huệ, Quận 1, TP.HCM");
            assertThat(company.getPhone()).isNull();
            assertThat(company.getEmail()).isNull();
            assertThat(company.getStatus()).isEqualTo(CompanyStatus.PENDING);
            assertThat(company.isApproved()).isFalse();
            assertThat(company.getSlug()).isNotBlank();

            boolean detailExists = companyEmployerDetailRepository.findAll().stream()
                    .anyMatch(d -> d.getCompany().getId().equals(company.getId())
                            && d.getId().equals(user.getId()));
            assertThat(detailExists).isTrue();
        }

        @Test
        void registerCompany_WithPasswordMismatch_Returns400() throws Exception {
            var payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "companyName", "Test Corp",
                    "taxCode", "0123456789",
                    "address", "456 Street",
                    "fullName", "HR Manager",
                    "userEmail", "test-hr@yoedu.com",
                    "userPhone", "0900000003",
                    "password", "password123",
                    "confirmPassword", "differentPassword"));

            mockMvc.perform(post("/api/auth/register/company")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Mật khẩu xác nhận không trùng"));
        }

        @Test
        void registerCompany_WithMissingFields_Returns400() throws Exception {
            var payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "companyName", "",
                    "address", "",
                    "fullName", "",
                    "userEmail", "",
                    "userPhone", "",
                    "password", "short",
                    "confirmPassword", ""));

            mockMvc.perform(post("/api/auth/register/company")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void registerCandidate_WithMissingFields_Returns400() throws Exception {
            var payload = objectMapper.writeValueAsString(java.util.Map.of(
                    "email", "not-an-email",
                    "fullName", "",
                    "password", "short",
                    "confirmPassword", ""));

            mockMvc.perform(post("/api/auth/register/candidate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isMap());
        }
    }

    // =========================================================================
    // Token lifecycle (login, refresh, logout)
    // =========================================================================

    @Nested
    class TokenLifecycleTest {

        @Test
        void TC_03_loginReturnsAccessAndRefreshTokens() throws Exception {
            User user = User.builder()
                    .email("test@example.com")
                    .password(passwordEncoder.encode("P@ssw0rd"))
                    .role(UserRole.CANDIDATE)
                    .isActive(true)
                    .build();
            userRepository.save(user);

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
            User user = User.builder()
                    .email("wrong@example.com")
                    .password(passwordEncoder.encode("CorrectPassword"))
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(user);

            var loginPayload = objectMapper.writeValueAsString(
                    java.util.Map.of("email", "wrong@example.com", "password", "WrongPassword"));

            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(loginPayload))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void TC_06_refreshFailsWithRevokedToken() throws Exception {
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
}