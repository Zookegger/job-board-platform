package com.yoedu.job_board_platform.controllers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.ApplicationStatusLogRepository;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ApplicationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired CompanyEmployerDetailRepository companyEmployerDetailRepository;
    @Autowired JobRepository jobRepository;
    @Autowired JobCategoryRepository jobCategoryRepository;
    @Autowired ApplicationRepository applicationRepository;
    @Autowired ApplicationStatusLogRepository applicationStatusLogRepository;
    @Autowired CandidateDetailRepository candidateDetailRepository;
    @Autowired CandidateSkillRepository candidateSkillRepository;
    @Autowired JobSkillRepository jobSkillRepository;
    @Autowired SkillRepository skillRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Job activeJob;
    private Job expiredJob;

    @BeforeEach
    void cleanup() {
        applicationStatusLogRepository.deleteAll();
        applicationRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        candidateSkillRepository.deleteAll();
        jobSkillRepository.deleteAll();
        skillRepository.deleteAll();
        jobRepository.deleteAll();
        jobCategoryRepository.deleteAll();
        companyEmployerDetailRepository.deleteAll();
        companyRepository.deleteAll();
        candidateDetailRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        JobCategory category = jobCategoryRepository.save(
                JobCategory.builder().name("CNTT").build());

        Company company = companyRepository.save(Company.builder()
                .companyName("Tech Corp")
                .slug("tech-corp-test")
                .address("Hà Nội")
                .description("Test company")
                .website("https://techcorp.vn")
                .logoUrl("")
                .email("hr@techcorp.vn")
                .phone("0900000001")
                .status(CompanyStatus.APPROVED)
                .taxCode("123456789")
                .isApproved(true)
                .createdAt(OffsetDateTime.now())
                .build());

        User employer = User.builder()
                .email("employer.test@techcorp.vn")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.EMPLOYER)
                .isActive(true)
                .build();
        Profile employerProfile = Profile.builder()
                .user(employer)
                .fullName("HR Manager")
                .phone("0900000002")
                .build();
        employer.setProfile(employerProfile);
        userRepository.save(employer);
        companyEmployerDetailRepository.save(CompanyEmployerDetail.builder()
                .profile(employerProfile)
                .company(company)
                .roleInCompany("HR")
                .build());

        activeJob = jobRepository.save(Job.builder()
                .company(company)
                .category(category)
                .title("Java Developer")
                .slug("java-developer-test")
                .description("Mô tả")
                .location("Hà Nội")
                .locationTypes(LocationTypes.ONSITE)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.MID)
                .salaryMin(new BigDecimal("15000000"))
                .salaryMax(new BigDecimal("25000000"))
                .numberOfOpenings(3)
                .status(JobStatus.ACTIVE)
                .postedDate(OffsetDateTime.now())
                .expirationDate(OffsetDateTime.now().plusDays(30))
                .build());

        expiredJob = jobRepository.save(Job.builder()
                .company(company)
                .category(category)
                .title("Expired Job")
                .slug("expired-job-test")
                .description("Mô tả")
                .location("Hà Nội")
                .locationTypes(LocationTypes.REMOTE)
                .employmentType(EmploymentType.CONTRACT)
                .experienceLevel(ExperienceLevel.JUNIOR)
                .salaryMin(new BigDecimal("10000000"))
                .salaryMax(new BigDecimal("15000000"))
                .numberOfOpenings(1)
                .status(JobStatus.EXPIRED)
                .postedDate(OffsetDateTime.now().minusDays(60))
                .expirationDate(OffsetDateTime.now().minusDays(1))
                .build());
    }

    /** Đăng ký candidate và trả về accessToken cookie */
    private Cookie registerAndLoginCandidate(String email) throws Exception {
        var registerPayload = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "fullName", "Ứng Viên Test",
                "password", "password123",
                "confirmPassword", "password123"));
        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        var loginPayload = objectMapper.writeValueAsString(
                Map.of("email", email, "password", "password123"));
        MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();
        return loginRes.getResponse().getCookie("accessToken");
    }

    // ----------------------------------------------------------------
    // US-28 TC-01: Nộp đơn thành công vào job ACTIVE
    // ----------------------------------------------------------------
    @Test
    void submitApplication_success() throws Exception {
        Cookie cookie = registerAndLoginCandidate("candidate.apply1@test.com");

        var payload = objectMapper.writeValueAsString(Map.of(
                "jobId", activeJob.getId().toString(),
                "coverLetter", "Kính gửi nhà tuyển dụng, tôi rất muốn ứng tuyển vị trí này."));

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(activeJob.getId().toString()))
                .andExpect(jsonPath("$.jobTitle").value("Java Developer"))
                .andExpect(jsonPath("$.companyName").value("Tech Corp"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.coverLetter").value("Kính gửi nhà tuyển dụng, tôi rất muốn ứng tuyển vị trí này."));

        assertThat(applicationRepository.count()).isEqualTo(1);
    }

    // ----------------------------------------------------------------
    // US-28 TC-02: Nộp đơn không có cover letter → vẫn thành công
    // ----------------------------------------------------------------
    @Test
    void submitApplication_noCoverLetter_success() throws Exception {
        Cookie cookie = registerAndLoginCandidate("candidate.apply2@test.com");

        var payload = objectMapper.writeValueAsString(Map.of(
                "jobId", activeJob.getId().toString()));

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // ----------------------------------------------------------------
    // US-28 TC-03: Nộp đơn 2 lần vào cùng job → 400
    // ----------------------------------------------------------------
    @Test
    void submitApplication_duplicate_returns400() throws Exception {
        Cookie cookie = registerAndLoginCandidate("candidate.apply3@test.com");

        var payload = objectMapper.writeValueAsString(Map.of(
                "jobId", activeJob.getId().toString()));

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        assertThat(applicationRepository.count()).isEqualTo(1);
    }

    // ----------------------------------------------------------------
    // US-28 TC-04: Nộp đơn vào job EXPIRED → 400
    // ----------------------------------------------------------------
    @Test
    void submitApplication_expiredJob_returns400() throws Exception {
        Cookie cookie = registerAndLoginCandidate("candidate.apply4@test.com");

        var payload = objectMapper.writeValueAsString(Map.of(
                "jobId", expiredJob.getId().toString()));

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        assertThat(applicationRepository.count()).isEqualTo(0);
    }

    // ----------------------------------------------------------------
    // US-28 TC-05: Nộp đơn khi chưa đăng nhập → 401
    // ----------------------------------------------------------------
    @Test
    void submitApplication_unauthenticated_returns401() throws Exception {
        var payload = objectMapper.writeValueAsString(Map.of(
                "jobId", activeJob.getId().toString()));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    // ----------------------------------------------------------------
    // US-28 TC-06: jobId không tồn tại → 404
    // ----------------------------------------------------------------
    @Test
    void submitApplication_jobNotFound_returns404() throws Exception {
        Cookie cookie = registerAndLoginCandidate("candidate.apply6@test.com");

        var payload = objectMapper.writeValueAsString(Map.of(
                "jobId", "00000000-0000-0000-0000-000000000000"));

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    // ----------------------------------------------------------------
    // US-28 TC-07: Thiếu jobId (validation) → 400
    // ----------------------------------------------------------------
    @Test
    void submitApplication_missingJobId_returns400() throws Exception {
        Cookie cookie = registerAndLoginCandidate("candidate.apply7@test.com");

        var payload = objectMapper.writeValueAsString(Map.of(
                "coverLetter", "No job ID provided"));

        mockMvc.perform(post("/api/applications")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
