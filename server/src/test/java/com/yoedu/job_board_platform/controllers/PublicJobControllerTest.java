package com.yoedu.job_board_platform.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.*;
import com.yoedu.job_board_platform.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PublicJobControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        UserRepository userRepository;
        @Autowired
        ProfileRepository profileRepository;
        @Autowired
        CompanyRepository companyRepository;
        @Autowired
        CompanyEmployerDetailRepository companyEmployerDetailRepository;
        @Autowired
        JobRepository jobRepository;
        @Autowired
        JobCategoryRepository jobCategoryRepository;
        @Autowired
        SkillRepository skillRepository;
        @Autowired
        JobSkillRepository jobSkillRepository;
        @Autowired
        CandidateDetailRepository candidateDetailRepository;
        @Autowired
        NotificationRepository notificationRepository;
        @Autowired
        RefreshTokenRepository refreshTokenRepository;
        @Autowired
        PasswordEncoder passwordEncoder;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private JobCategory savedCategory;
        private Company savedCompany;

        private void createActiveJob(String title, String slug, OffsetDateTime expirationDate) {
                Job job = Job.builder()
                        .title(title)
                        .slug(slug)
                        .description("Job description for " + title)
                        .company(savedCompany)
                        .category(savedCategory)
                        .status(JobStatus.ACTIVE)
                        .locationTypes(LocationTypes.ONSITE)
                        .employmentType(EmploymentType.FULL_TIME)
                        .experienceLevel(ExperienceLevel.MID)
                        .salaryMin(new BigDecimal("10000000"))
                        .salaryMax(new BigDecimal("20000000"))
                        .currency("VND")
                        .postedDate(OffsetDateTime.now().minusDays(1))
                        .expirationDate(expirationDate)
                        .numberOfOpenings(1)
                        .build();
                jobRepository.save(job);
        }

        @BeforeEach
        void cleanup() {
                jobSkillRepository.deleteAll();
                jobRepository.deleteAll();
                jobCategoryRepository.deleteAll();
                skillRepository.deleteAll();
                candidateDetailRepository.deleteAll();
                companyEmployerDetailRepository.deleteAll();
                companyRepository.deleteAll();
                profileRepository.deleteAll();
                notificationRepository.deleteAll();
                refreshTokenRepository.deleteAll();
                userRepository.deleteAll();

                savedCategory = jobCategoryRepository.save(
                        JobCategory.builder().name("IT").build());

                User employerUser = User.builder()
                        .email("employer-public@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(UserRole.EMPLOYER)
                        .isActive(true)
                        .build();
                userRepository.save(employerUser);

                Profile profile = Profile.builder()
                        .user(employerUser)
                        .fullName("Employer Test")
                        .phone("0924913125")
                        .build();
                profileRepository.save(profile);

                employerUser.setProfile(profile);
                userRepository.save(employerUser);

                savedCompany = companyRepository.save(
                        Company.builder()
                                .companyName("Test Corp Public")
                                .slug("test-corp-public")
                                .address("123 Street")
                                .build());

                CompanyEmployerDetail employerDetail = CompanyEmployerDetail.builder()
                        .profile(profile)
                        .company(savedCompany)
                        .build();
                companyEmployerDetailRepository.save(employerDetail);
        }

        @Test
        void getCategories_returnsAllCategories() throws Exception {
                jobCategoryRepository.save(JobCategory.builder().name("Finance").build());

                var res = mockMvc.perform(get("/api/categories"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json).hasSize(2);
                assertThat(json.get(0).get("name").asText()).isEqualTo("IT");
                assertThat(json.get(1).get("name").asText()).isEqualTo("Finance");
        }

        @Test
        void getCategories_whenEmpty_returnsEmptyList() throws Exception {
                jobCategoryRepository.deleteAll();

                var res = mockMvc.perform(get("/api/categories"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json).isEmpty();
        }

        @Test
        void searchPublicJobs_withKeyword_returnsMatchingJobs() throws Exception {
                createActiveJob("Software Engineer", "software-engineer", OffsetDateTime.now().plusDays(30));
                createActiveJob("Data Analyst", "data-analyst", OffsetDateTime.now().plusDays(30));

                var res = mockMvc.perform(get("/api/jobs/public")
                        .param("keyword", "Software")
                        .param("size", "12"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("title").asText()).isEqualTo("Software Engineer");
        }

        @Test
        void searchPublicJobs_whenNoMatch_returnsEmptyPage() throws Exception {
                createActiveJob("Software Engineer", "software-engineer", OffsetDateTime.now().plusDays(30));

                var res = mockMvc.perform(get("/api/jobs/public")
                        .param("keyword", "NonExistentJobXYZ")
                        .param("size", "12"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("content")).isEmpty();
                assertThat(json.get("totalElements").asInt()).isEqualTo(0);
        }

        @Test
        void searchPublicJobs_excludesExpiredJobs() throws Exception {
                createActiveJob("Active Job", "active-job", OffsetDateTime.now().plusDays(30));
                createActiveJob("Expired Job", "expired-job", OffsetDateTime.now().minusDays(1));

                var res = mockMvc.perform(get("/api/jobs/public")
                        .param("size", "12"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("title").asText()).isEqualTo("Active Job");
        }

        @Test
        void searchPublicJobs_pagination_returnsCorrectPageSize() throws Exception {
                for (int i = 0; i < 15; i++) {
                        createActiveJob("Bulk Job " + i, "bulk-job-" + i, OffsetDateTime.now().plusDays(10));
                }

                var res = mockMvc.perform(get("/api/jobs/public")
                                .param("page", "0")
                                .param("size", "10"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("content")).hasSize(10);
                assertThat(json.get("totalElements").asInt()).isEqualTo(15);
                assertThat(json.get("totalPages").asInt()).isEqualTo(2);
        }

        @Test
        void getJobBySlug_whenJobExistsAndActive_returnsJobDetails() throws Exception {
                createActiveJob("DevOps Engineer", "devops-engineer", OffsetDateTime.now().plusDays(30));

                var res = mockMvc.perform(get("/api/jobs/public/devops-engineer"))
                        .andExpect(status().isOk())
                        .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("DevOps Engineer");
                assertThat(json.get("status").asText()).isEqualTo("ACTIVE");
        }

        @Test
        void getJobBySlug_whenJobExpired_returnsOk() throws Exception {
                createActiveJob("Legacy Dev", "legacy-dev", OffsetDateTime.now().minusDays(5));

                mockMvc.perform(get("/api/jobs/public/legacy-dev"))
                        .andExpect(status().isOk());
        }
}
