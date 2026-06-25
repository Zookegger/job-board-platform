package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.utils.DatabaseCleaner;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ApplicationControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        JdbcTemplate jdbcTemplate;
        @Autowired
        EntityManager entityManager;
        @Autowired
        UserRepository userRepository;
        @Autowired
        JobRepository jobRepository;
        @Autowired
        JobCategoryRepository jobCategoryRepository;
        @Autowired
        ApplicationRepository applicationRepository;
        @Autowired
        PasswordEncoder passwordEncoder;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private JobCategory savedCategory;

        @BeforeEach
        void cleanup() {
                DatabaseCleaner.cleanAllTables(jdbcTemplate, entityManager);

                savedCategory = jobCategoryRepository.save(
                                JobCategory.builder().name("IT").build());
        }

        private Cookie registerAndLoginEmployer(String email, String password) throws Exception {
                var registerPayload = objectMapper.writeValueAsString(Map.of(
                                "companyName", "Test Corp " + email.hashCode(),
                                "taxCode", "0123456789" + Math.abs(email.hashCode() % 100000000),
                                "address", "123 Street",
                                "fullName", "Trần Thị B",
                                "userEmail", email,
                                "userPhone", "0900000001",
                                "password", password,
                                "confirmPassword", password));
                mockMvc.perform(post("/api/auth/register/company")
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

        private Cookie registerAndLoginCandidate(String email, String password) throws Exception {
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

        private Job createActiveJobForEmployer(String employerEmail) {
                User employer = userRepository.findByEmail(employerEmail).orElseThrow();
                var company = employer.getProfile().getEmployerDetail().getCompany();

                return jobRepository.save(Job.builder()
                                .title("Senior Java Developer")
                                .slug("senior-java-developer-" + UUID.randomUUID())
                                .description("Job description")
                                .company(company)
                                .category(savedCategory)
                                .location("Hà Nội")
                                .locationTypes(com.yoedu.job_board_platform.models.LocationTypes.ONSITE)
                                .employmentType(com.yoedu.job_board_platform.models.EmploymentType.FULL_TIME)
                                .experienceLevel(com.yoedu.job_board_platform.models.ExperienceLevel.MID)
                                .status(JobStatus.ACTIVE)
                                .build());
        }

        private Application createApplicationForCandidate(String candidateEmail, Job job, ApplicationStatus status) {
                User candidate = userRepository.findByEmail(candidateEmail).orElseThrow();
                Profile profile = candidate.getProfile();

                return applicationRepository.save(Application.builder()
                                .candidate(profile)
                                .job(job)
                                .status(status)
                                .appliedAt(OffsetDateTime.now())
                                .build());
        }

        @Test
        void candidate_listApplications_noApplications_returnsEmpty() throws Exception {
                Cookie tokenCookie = registerAndLoginCandidate("candidate-apps@test.com", "password123");

                MvcResult res = mockMvc.perform(get("/api/applications")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(0);
                assertThat(json.get("content").isArray()).isTrue();
                assertThat(json.get("content").isEmpty()).isTrue();
        }

        @Test
        void candidate_listApplications_returnsApplications() throws Exception {
                registerAndLoginEmployer("employer-apps@test.com", "password123");
                Job job = createActiveJobForEmployer("employer-apps@test.com");

                Cookie tokenCookie = registerAndLoginCandidate("candidate-apps2@test.com", "password123");
                createApplicationForCandidate("candidate-apps2@test.com", job, ApplicationStatus.PENDING);

                MvcResult res = mockMvc.perform(get("/api/applications")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("jobTitle").asText()).isEqualTo("Senior Java Developer");
                assertThat(json.get("content").get(0).get("status").asText()).isEqualTo("PENDING");
        }

        @Test
        void candidate_listApplications_filterByStatus_returnsFiltered() throws Exception {
                registerAndLoginEmployer("employer-filter-apps@test.com", "password123");
                Job job1 = createActiveJobForEmployer("employer-filter-apps@test.com");

                Job job2 = jobRepository.save(Job.builder()
                                .title("Frontend Developer")
                                .slug("frontend-developer-" + UUID.randomUUID())
                                .description("Job description")
                                .company(job1.getCompany())
                                .category(savedCategory)
                                .locationTypes(com.yoedu.job_board_platform.models.LocationTypes.REMOTE)
                                .employmentType(com.yoedu.job_board_platform.models.EmploymentType.FULL_TIME)
                                .experienceLevel(com.yoedu.job_board_platform.models.ExperienceLevel.JUNIOR)
                                .status(JobStatus.ACTIVE)
                                .build());

                Cookie tokenCookie = registerAndLoginCandidate("candidate-filter-apps@test.com", "password123");
                createApplicationForCandidate("candidate-filter-apps@test.com", job1, ApplicationStatus.PENDING);
                createApplicationForCandidate("candidate-filter-apps@test.com", job2, ApplicationStatus.REVIEWING);

                MvcResult pendingRes = mockMvc.perform(get("/api/applications")
                                .cookie(tokenCookie)
                                .param("status", "PENDING"))
                                .andExpect(status().isOk())
                                .andReturn();
                var pendingJson = objectMapper.readTree(pendingRes.getResponse().getContentAsString());
                assertThat(pendingJson.get("totalElements").asInt()).isEqualTo(1);
                assertThat(pendingJson.get("content").get(0).get("status").asText()).isEqualTo("PENDING");

                MvcResult reviewingRes = mockMvc.perform(get("/api/applications")
                                .cookie(tokenCookie)
                                .param("status", "REVIEWING"))
                                .andExpect(status().isOk())
                                .andReturn();
                var reviewingJson = objectMapper.readTree(reviewingRes.getResponse().getContentAsString());
                assertThat(reviewingJson.get("totalElements").asInt()).isEqualTo(1);
                assertThat(reviewingJson.get("content").get(0).get("status").asText()).isEqualTo("REVIEWING");
        }

        @Test
        void candidate_listApplications_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/applications"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void candidate_listApplications_asEmployer_returns403() throws Exception {
                Cookie tokenCookie = registerAndLoginEmployer("employer-no-apps@test.com", "password123");

                mockMvc.perform(get("/api/applications")
                                .cookie(tokenCookie))
                                .andExpect(status().isForbidden());
        }
}
