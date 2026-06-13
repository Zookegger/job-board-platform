package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.JsonNode;
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
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    CompanyEmployerDetailRepository employerDetailRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        jobRepository.deleteAll();
        employerDetailRepository.deleteAll();
        companyRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Cookie loginAsAdmin() throws Exception {
        User admin = User.builder()
                .email("admin-test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);

        var loginPayload = objectMapper.writeValueAsString(Map.of(
                "email", admin.getEmail(),
                "password", "password123"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult.getResponse().getCookie("accessToken");
    }

    private Company createPendingCompany(String companyName, String taxCode, String email, String phone) {
        Company company = companyRepository.save(Company.builder()
                .companyName(companyName)
                .slug(companyName.toLowerCase().replace(' ', '-'))
                .address("123 Address")
                .description("Test company")
                .website("https://example.com")
                .logoUrl("https://example.com/logo.png")
                .email(email)
                .phone(phone)
                .status(CompanyStatus.PENDING)
                .taxCode(taxCode)
                .isApproved(false)
                .createdAt(OffsetDateTime.now())
                .build());

        User employer = User.builder()
                .email("employer." + email)
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .build();

        Profile profile = Profile.builder()
                .user(employer)
                .fullName("Test " + companyName)
                .phone(phone)
                .build();

        employer.setProfile(profile);
        userRepository.save(employer);

        employerDetailRepository.save(CompanyEmployerDetail.builder()
                .profile(profile)
                .company(company)
                .roleInCompany("HR")
                .build());

        return company;
    }

    @Test
    void admin_canListPendingCompanies() throws Exception {
        createPendingCompany("Pending One", "111111111", "one@example.com", "0900000001");
        createPendingCompany("Pending Two", "222222222", "two@example.com", "0900000002");

        Cookie adminCookie = loginAsAdmin();

        MvcResult result = mockMvc.perform(get("/api/admin/companies/pending")
                        .cookie(adminCookie)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("content").isArray()).isTrue();
        assertThat(json.get("totalElements").asInt()).isEqualTo(2);
        assertThat(json.get("content").get(0).get("companyName").asText()).isNotEmpty();
    }

    @Test
    void admin_canApprovePendingCompany() throws Exception {
        Company company = createPendingCompany("Approve Corp", "333333333", "approve@example.com", "0900000003");
        Cookie adminCookie = loginAsAdmin();

        mockMvc.perform(post("/api/admin/companies/" + company.getId() + "/approve")
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Duyệt công ty thành công"));

        Company updated = companyRepository.findById(company.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CompanyStatus.APPROVED);
        assertThat(updated.isApproved()).isTrue();
        assertThat(updated.getRejectionReason()).isNull();
    }

    @Test
    void admin_canRejectPendingCompany_withReason() throws Exception {
        Company company = createPendingCompany("Reject Corp", "444444444", "reject@example.com", "0900000004");
        Cookie adminCookie = loginAsAdmin();

        mockMvc.perform(post("/api/admin/companies/" + company.getId() + "/reject")
                        .cookie(adminCookie)
                        .param("reason", "Thông tin không hợp lệ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Từ chối công ty thành công"));

        Company updated = companyRepository.findById(company.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CompanyStatus.REJECTED);
        assertThat(updated.isApproved()).isFalse();
        assertThat(updated.getRejectionReason()).isEqualTo("Thông tin không hợp lệ");

        MvcResult result = mockMvc.perform(get("/api/admin/companies/pending")
                        .cookie(adminCookie)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("totalElements").asInt()).isEqualTo(0);
    }
}
