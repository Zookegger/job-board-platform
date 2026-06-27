package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

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
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.ReportStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.ReportRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ReportControllerTest {

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
    JobRepository jobRepository;

    @Autowired
    JobCategoryRepository jobCategoryRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JobCategory savedCategory;

    @BeforeEach
    void cleanup() {
        reportRepository.deleteAll();
        employerDetailRepository.deleteAll();
        jobRepository.deleteAll();
        jobCategoryRepository.deleteAll();
        companyRepository.deleteAll();
        profileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        savedCategory = jobCategoryRepository.save(
                JobCategory.builder().name("IT").build());
    }

    private Cookie registerAndLogin(String email, String password) throws Exception {
        var registerPayload = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "fullName", "Nguyễn Văn A",
                "password", password,
                "confirmPassword", password));
        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        var loginPayload = objectMapper.writeValueAsString(
                Map.of("email", email, "password", password));
        MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();
        return loginRes.getResponse().getCookie("accessToken");
    }

    private Company createApprovedCompany(String name) {
        Company company = companyRepository.save(Company.builder()
                .companyName(name)
                .slug(name.toLowerCase().replace(' ', '-'))
                .address("123 Street")
                .description("Test company")
                .status(CompanyStatus.APPROVED)
                .isApproved(true)
                .createdAt(OffsetDateTime.now())
                .build());

        User employer = User.builder()
                .email("employer." + name + "@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .build();

        Profile profile = Profile.builder()
                .user(employer)
                .fullName("Employer " + name)
                .phone("0900000099")
                .build();

        employer.setProfile(profile);
        userRepository.save(employer);

        return company;
    }

    private Job createActiveJob(Company company, String title) {
        return jobRepository.save(Job.builder()
                .company(company)
                .category(savedCategory)
                .title(title)
                .slug(title.toLowerCase().replace(' ', '-') + "-" + System.currentTimeMillis())
                .description("Description for " + title)
                .location("Hà Nội")
                .locationTypes(LocationTypes.ONSITE)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .status(JobStatus.ACTIVE)
                .numberOfOpenings(2)
                .build());
    }

    // ----------------------------------------------------------------
    // POST /api/reports — createReport
    // ----------------------------------------------------------------

    @Test
    void createReport_forJob_success() throws Exception {
        Company company = createApprovedCompany("Reported Job Corp");
        Job job = createActiveJob(company, "Engineer");
        Cookie tokenCookie = registerAndLogin("reporter@test.com", "password123");

        String payload = objectMapper.writeValueAsString(Map.of(
                "jobId", job.getId().toString(),
                "reason", "SPAM"));

        MvcResult result = mockMvc.perform(post("/api/reports")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("jobId").asText()).isEqualTo(job.getId().toString());
        assertThat(json.get("jobTitle").asText()).isEqualTo("Engineer");
        assertThat(json.get("reason").asText()).isEqualTo("SPAM");
        assertThat(json.get("status").asText()).isEqualTo(ReportStatus.PENDING.name());
        assertThat(json.get("reportedById")).isNotNull();
        assertThat(json.get("createdAt")).isNotNull();
    }

    @Test
    void createReport_forCompany_success() throws Exception {
        Company company = createApprovedCompany("Reported Company Corp");
        Cookie tokenCookie = registerAndLogin("reporter2@test.com", "password123");

        String payload = objectMapper.writeValueAsString(Map.of(
                "companyId", company.getId().toString(),
                "reason", "SCAM",
                "details", "This company looks suspicious"));

        MvcResult result = mockMvc.perform(post("/api/reports")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("companyId").asText()).isEqualTo(company.getId().toString());
        assertThat(json.get("companyName").asText()).isEqualTo("Reported Company Corp");
        assertThat(json.get("reason").asText()).isEqualTo("SCAM");
        assertThat(json.get("details").asText()).isEqualTo("This company looks suspicious");
        assertThat(json.get("status").asText()).isEqualTo(ReportStatus.PENDING.name());
    }

    @Test
    void createReport_withBothTargets_returns400() throws Exception {
        Company company = createApprovedCompany("Both Corp");
        Job job = createActiveJob(company, "Both Job");
        Cookie tokenCookie = registerAndLogin("both@test.com", "password123");

        String payload = objectMapper.writeValueAsString(Map.of(
                "jobId", job.getId().toString(),
                "companyId", company.getId().toString(),
                "reason", "SPAM"));

        mockMvc.perform(post("/api/reports")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReport_withNoTarget_returns400() throws Exception {
        Cookie tokenCookie = registerAndLogin("notarget@test.com", "password123");

        String payload = objectMapper.writeValueAsString(Map.of(
                "reason", "SPAM"));

        mockMvc.perform(post("/api/reports")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReport_withInvalidJobId_returns404() throws Exception {
        Cookie tokenCookie = registerAndLogin("invalid@test.com", "password123");
        UUID fakeId = UUID.randomUUID();

        String payload = objectMapper.writeValueAsString(Map.of(
                "jobId", fakeId.toString(),
                "reason", "SPAM"));

        mockMvc.perform(post("/api/reports")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReport_withoutAuth_returns401() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "reason", "SPAM"));

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReport_withInvalidReason_returns400() throws Exception {
        Cookie tokenCookie = registerAndLogin("badreason@test.com", "password123");

        String payload = objectMapper.writeValueAsString(Map.of(
                "reason", "INVALID_REASON"));

        mockMvc.perform(post("/api/reports")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
