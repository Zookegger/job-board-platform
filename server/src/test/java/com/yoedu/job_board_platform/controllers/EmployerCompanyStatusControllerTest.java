package com.yoedu.job_board_platform.controllers;

import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class EmployerCompanyStatusControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CompanyEmployerDetailRepository employerDetailRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        employerDetailRepository.deleteAll();
        companyRepository.deleteAll();
        profileRepository.deleteAll();
        notificationRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private record EmployerContext(User user, Company company, Cookie cookie) {}

    private EmployerContext createEmployerWithCompany(CompanyStatus status, String suffix) throws Exception {
        Company company = companyRepository.save(Company.builder()
                .companyName("Test Corp " + suffix)
                .slug("test-corp-" + suffix)
                .address("123 Test Street")
                .description("Mô tả công ty")
                .status(status)
                .taxCode("01234567" + suffix)
                .isApproved(status == CompanyStatus.APPROVED)
                .createdAt(OffsetDateTime.now().minusDays(5))
                .build());

        User employer = userRepository.save(User.builder()
                .email("employer" + suffix + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .build());

        Profile profile = profileRepository.save(Profile.builder()
                .user(employer)
                .fullName("Test Employer " + suffix)
                .phone("090000000" + suffix)
                .build());

        employerDetailRepository.save(CompanyEmployerDetail.builder()
                .profile(profile)
                .company(company)
                .roleInCompany("OWNER")
                .build());

        Cookie cookie = loginAs(employer.getEmail(), "password123");
        return new EmployerContext(employer, company, cookie);
    }

    private Cookie loginAs(String email, String password) throws Exception {
        var payload = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("accessToken");
    }

    // ── GET /api/company/status ───────────────────────────────────────────────

    @Test
    void getStatus_WithValidEmployerToken_Returns200AndPendingStatus() throws Exception {
        EmployerContext ctx = createEmployerWithCompany(CompanyStatus.PENDING, "1");

        mockMvc.perform(get("/api/company/status")
                        .cookie(ctx.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.name").value("Test Corp 1"))
                .andExpect(jsonPath("$.companyId").isNotEmpty());
    }

    @Test
    void getStatus_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/company/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStatus_WithAdminRole_Returns403() throws Exception {
        User admin = userRepository.save(User.builder()
                .email("admin-status@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build());
        Cookie adminCookie = loginAs(admin.getEmail(), "password123");

        mockMvc.perform(get("/api/company/status")
                        .cookie(adminCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStatus_WhenNoCompany_Returns404() throws Exception {
        // Employer không có company → ResourceNotFoundException
        User employer = userRepository.save(User.builder()
                .email("no-company@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .build());
        profileRepository.save(Profile.builder()
                .user(employer)
                .fullName("No Company Employer")
                .phone("0900000099")
                .build());

        Cookie cookie = loginAs(employer.getEmail(), "password123");

        mockMvc.perform(get("/api/company/status")
                        .cookie(cookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ── GET /api/company/approval-history ─────────────────────────────────────

    @Test
    void getHistory_WithValidToken_ReturnsEmptyList() throws Exception {
        EmployerContext ctx = createEmployerWithCompany(CompanyStatus.APPROVED, "2");

        mockMvc.perform(get("/api/company/approval-history")
                        .cookie(ctx.cookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getHistory_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/company/approval-history"))
                .andExpect(status().isUnauthorized());
    }
}
