package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RegistrationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyEmployerDetailRepository companyEmployerDetailRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        companyEmployerDetailRepository.deleteAll();
        companyRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

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
                "companyPhone", "0901234567",
                "email", "careers@yoedu.com",
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

        Company company = companyRepository.findByEmail("careers@yoedu.com");
        assertThat(company).isNotNull();
        assertThat(company.getCompanyName()).isEqualTo("Yoedu Technology Corporation");
        assertThat(company.getAddress()).isEqualTo("123 Nguyễn Huệ, Quận 1, TP.HCM");
        assertThat(company.getPhone()).isEqualTo("0901234567");
        assertThat(company.getStatus()).isEqualTo(CompanyStatus.PENDING);
        assertThat(company.isApproved()).isFalse();
        assertThat(company.getSlug()).isNotBlank();

        boolean detailExists = companyEmployerDetailRepository.findAll().stream()
                .anyMatch(d -> d.getCompany().getId().equals(company.getId())
                        && d.getId().equals(user.getId()));
        assertThat(detailExists).isTrue();
    }

    @Test
    void registerCompany_WithDuplicateEmail_Returns409() throws Exception {
        var payload = objectMapper.writeValueAsString(java.util.Map.of(
                "companyName", "Yoedu Corp",
                "address", "123 Street",
                "companyPhone", "0900000000",
                "email", "duplicate@yoedu.com",
                "fullName", "HR Manager",
                "userEmail", "hr@yoedu.com",
                "userPhone", "0900000001",
                "password", "password123",
                "confirmPassword", "password123"));

        mockMvc.perform(post("/api/auth/register/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email duplicate@yoedu.com đã được sử dụng bởi công ty khác"));
    }

    @Test
    void registerCompany_WithPasswordMismatch_Returns400() throws Exception {
        var payload = objectMapper.writeValueAsString(java.util.Map.of(
                "companyName", "Test Corp",
                "address", "456 Street",
                "companyPhone", "0900000002",
                "email", "test@yoedu.com",
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
                "companyPhone", "",
                "email", "invalid",
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
