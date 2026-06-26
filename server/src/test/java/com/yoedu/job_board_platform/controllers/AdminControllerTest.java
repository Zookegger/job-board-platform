package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.ReportReason;
import com.yoedu.job_board_platform.models.ReportStatus;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.CompanyApprovalLogRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.ReportRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;

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

        @Autowired
        SkillRepository skillRepository;

        @Autowired
        JobSkillRepository jobSkillRepository;

        @Autowired
        CandidateSkillRepository candidateSkillRepository;

        @Autowired
        NotificationRepository notificationRepository;

        @Autowired
        ReportRepository reportRepository;

        @Autowired
        RefreshTokenRepository refreshTokenRepository;

        @Autowired
        JobCategoryRepository jobCategoryRepository;

        @Autowired
        CompanyApprovalLogRepository companyApprovalLogRepository;

        @Autowired
        ApplicationRepository applicationRepository;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private Skill savedSkillActive;
        private Skill savedSkillInactive;
        private JobCategory savedCategory;

        @BeforeEach
        void cleanup() {
                candidateSkillRepository.deleteAll();
                jobSkillRepository.deleteAll();
                skillRepository.deleteAll();
                reportRepository.deleteAll();
                applicationRepository.deleteAll();
                jobRepository.deleteAll();
                jobCategoryRepository.deleteAll();
                notificationRepository.deleteAll();
                employerDetailRepository.deleteAll();
                companyApprovalLogRepository.deleteAll();
                companyRepository.deleteAll();
                profileRepository.deleteAll();
                refreshTokenRepository.deleteAll();
                userRepository.deleteAll();

                savedSkillActive = skillRepository.save(
                                Skill.builder().name("Java").isActive(true).build());
                savedSkillInactive = skillRepository.save(
                                Skill.builder().name("Kotlin").isActive(false).build());

                savedCategory = jobCategoryRepository.save(
                                JobCategory.builder().name("Công nghệ thông tin").build());
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

        private Job createPendingJob(Company company, String title) {
                return jobRepository.save(Job.builder()
                                .company(company)
                                .category(savedCategory)
                                .title(title)
                                .slug(title.toLowerCase().replace(' ', '-') + "-" + System.currentTimeMillis())
                                .description("Mô tả " + title)
                                .location("Hà Nội")
                                .locationTypes(LocationTypes.ONSITE)
                                .employmentType(EmploymentType.FULL_TIME)
                                .experienceLevel(ExperienceLevel.JUNIOR)
                                .salaryMin(new java.math.BigDecimal("10000000"))
                                .salaryMax(new java.math.BigDecimal("20000000"))
                                .numberOfOpenings(2)
                                .status(JobStatus.PENDING_APPROVAL)
                                .build());
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

        private Profile createCandidateProfile(String email) {
                User candidate = User.builder()
                                .email(email)
                                .password(passwordEncoder.encode("password123"))
                                .role(UserRole.CANDIDATE)
                                .isActive(true)
                                .build();

                Profile profile = Profile.builder()
                                .user(candidate)
                                .fullName("Candidate Test")
                                .phone("0900000099")
                                .build();

                candidate.setProfile(profile);
                userRepository.save(candidate);
                return profile;
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
                Company company = createPendingCompany("Approve Corp", "333333333", "approve@example.com",
                                "0900000003");
                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/companies/" + company.getId() + "/approve")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"Phê duyệt\"}"))
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

                mockMvc.perform(patch("/api/admin/companies/" + company.getId() + "/reject")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"Thông tin không hợp lệ\"}"))
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

        // ----------------------------------------------------------------
        // Admin Skills — GET /api/admin/skills
        // ----------------------------------------------------------------

        @Test
        void admin_canListAllSkills() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                MvcResult result = mockMvc.perform(get("/api/admin/skills")
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(json.get("content").isArray()).isTrue();
                assertThat(json.get("content").size()).isEqualTo(2); // active + inactive
                assertThat(json.get("totalElements").asInt()).isEqualTo(2);
        }

        @Test
        void listSkills_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/admin/skills"))
                                .andExpect(status().isUnauthorized());
        }

        // ----------------------------------------------------------------
        // Admin Skills — POST /api/admin/skills
        // ----------------------------------------------------------------

        @Test
        void admin_canCreateSkill() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                String payload = objectMapper.writeValueAsString(Map.of(
                                "name", "Rust",
                                "isActive", true));

                mockMvc.perform(post("/api/admin/skills")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Rust"))
                                .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        void admin_createSkill_duplicateName_returns409() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                String payload = objectMapper.writeValueAsString(Map.of(
                                "name", "Java",
                                "isActive", true));

                mockMvc.perform(post("/api/admin/skills")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isConflict());
        }

        // ----------------------------------------------------------------
        // Admin Skills — PUT /api/admin/skills/{id}
        // ----------------------------------------------------------------

        @Test
        void admin_canUpdateSkill() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                String payload = objectMapper.writeValueAsString(Map.of(
                                "name", "Java 8",
                                "isActive", true));

                mockMvc.perform(put("/api/admin/skills/" + savedSkillActive.getId())
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Java 8"));
        }

        // ----------------------------------------------------------------
        // Admin Skills — PUT /api/admin/skills/{id}/toggle-status
        // ----------------------------------------------------------------

        @Test
        void admin_canToggleSkillStatus() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/skills/" + savedSkillActive.getId() + "/toggle-status")
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isActive").value(false)); // was active → becomes inactive
        }

        // ----------------------------------------------------------------
        // Admin Skills — DELETE /api/admin/skills/{id}
        // ----------------------------------------------------------------

        @Test
        void admin_canDeleteSkill() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(delete("/api/admin/skills/" + savedSkillInactive.getId())
                                .cookie(adminCookie))
                                .andExpect(status().isOk());

                assertThat(skillRepository.findById(savedSkillInactive.getId())).isEmpty();
        }

        @Test
        void admin_deleteSkill_notFound_returns404() throws Exception {
                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(delete("/api/admin/skills/9999")
                                .cookie(adminCookie))
                                .andExpect(status().isNotFound());
        }

        // ----------------------------------------------------------------
        // Admin Jobs — GET /api/admin/jobs
        // ----------------------------------------------------------------

        @Test
        void admin_canListAllJobs() throws Exception {
                Company company = createPendingCompany("Jobs Corp", "555555555", "jobs@example.com", "0900000005");
                createPendingJob(company, "Software Engineer");
                createPendingJob(company, "Product Manager");

                Cookie adminCookie = loginAsAdmin();

                MvcResult result = mockMvc.perform(get("/api/admin/jobs")
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(json.get("content").isArray()).isTrue();
                assertThat(json.get("totalElements").asInt()).isEqualTo(2);
        }

        @Test
        void admin_canFilterJobsByStatus() throws Exception {
                Company company = createPendingCompany("Filter Corp", "666666666", "filter@example.com", "0900000006");
                createPendingJob(company, "Pending Job");
                Job activeJob = createPendingJob(company, "Active Job");
                activeJob.setStatus(JobStatus.ACTIVE);
                jobRepository.save(activeJob);

                Cookie adminCookie = loginAsAdmin();

                MvcResult result = mockMvc.perform(get("/api/admin/jobs")
                                .cookie(adminCookie)
                                .param("status", "PENDING_APPROVAL"))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("status").asText()).isEqualTo("PENDING_APPROVAL");
        }

        @Test
        void getAllJobs_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/admin/jobs"))
                                .andExpect(status().isUnauthorized());
        }

        // ----------------------------------------------------------------
        // Admin Jobs — GET /api/admin/jobs/pending
        // ----------------------------------------------------------------

        @Test
        void admin_canListPendingJobs() throws Exception {
                Company company = createPendingCompany("Pending Jobs Corp", "777777777", "pendingjobs@example.com",
                                "0900000007");
                createPendingJob(company, "Pending Engineer");
                Job activeJob = createPendingJob(company, "Active Engineer");
                activeJob.setStatus(JobStatus.ACTIVE);
                jobRepository.save(activeJob);

                Cookie adminCookie = loginAsAdmin();

                MvcResult result = mockMvc.perform(get("/api/admin/jobs/pending")
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("title").asText()).isEqualTo("Pending Engineer");
        }

        @Test
        void getPendingJobs_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/admin/jobs/pending"))
                                .andExpect(status().isUnauthorized());
        }

        // ----------------------------------------------------------------
        // Admin Jobs — PATCH /api/admin/jobs/{id}/approve
        // ----------------------------------------------------------------

        @Test
        void admin_canApprovePendingJob() throws Exception {
                Company company = createPendingCompany("Approve Job Corp", "888888888", "approvejob@example.com",
                                "0900000008");
                Job job = createPendingJob(company, "Approvable Engineer");

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/jobs/" + job.getId() + "/approve")
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Duyệt tin tuyển dụng thành công"));

                Job updated = jobRepository.findById(job.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(JobStatus.ACTIVE);
                assertThat(updated.getRejectionReason()).isNull();
        }

        @Test
        void admin_approveNonPendingJob_returns400() throws Exception {
                Company company = createPendingCompany("NonPending Corp", "999999999", "nonpending@example.com",
                                "0900000009");
                Job job = createPendingJob(company, "Non Pending");
                job.setStatus(JobStatus.DRAFT);
                jobRepository.save(job);

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/jobs/" + job.getId() + "/approve")
                                .cookie(adminCookie))
                                .andExpect(status().isBadRequest());
        }

        // ----------------------------------------------------------------
        // Admin Jobs — PATCH /api/admin/jobs/{id}/reject
        // ----------------------------------------------------------------

        @Test
        void admin_canRejectPendingJob_withReason() throws Exception {
                Company company = createPendingCompany("Reject Job Corp", "101010101", "rejectjob@example.com",
                                "0900000010");
                Job job = createPendingJob(company, "Rejectable Engineer");

                Cookie adminCookie = loginAsAdmin();

                String payload = objectMapper.writeValueAsString(Map.of("reason", "Nội dung không phù hợp"));

                mockMvc.perform(patch("/api/admin/jobs/" + job.getId() + "/reject")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Từ chối tin tuyển dụng thành công"));

                Job updated = jobRepository.findById(job.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(JobStatus.REJECTED);
                assertThat(updated.getRejectionReason()).isEqualTo("Nội dung không phù hợp");
        }

        @Test
        void admin_rejectJob_withoutReason_returns400() throws Exception {
                Company company = createPendingCompany("No Reason Corp", "111111112", "noreason@example.com",
                                "0900000011");
                Job job = createPendingJob(company, "No Reason Job");

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/jobs/" + job.getId() + "/reject")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        // ----------------------------------------------------------------
        // Admin Jobs — DELETE /api/admin/jobs/{id}
        // ----------------------------------------------------------------

        @Test
        void admin_canDeleteJob() throws Exception {
                Company company = createPendingCompany("Delete Job Corp", "121212121", "deletejob@example.com",
                                "0900000012");
                Job job = createPendingJob(company, "Deletable Engineer");

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(delete("/api/admin/jobs/" + job.getId())
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Xóa tin thành công"));

                assertThat(jobRepository.findById(job.getId())).isPresent();
        }

        @Test
        void deleteJob_withoutAuth_returns401() throws Exception {
                mockMvc.perform(delete("/api/admin/jobs/some-id"))
                                .andExpect(status().isUnauthorized());
        }

        // ----------------------------------------------------------------
        // Admin Reports — GET /api/admin/reports
        // ----------------------------------------------------------------

        private Report createReport(Job job, ReportReason reason) {
                User candidate = User.builder()
                                .email("candidate.report." + System.nanoTime() + "@test.com")
                                .password(passwordEncoder.encode("password123"))
                                .role(UserRole.CANDIDATE)
                                .isActive(true)
                                .build();

                Profile profile = Profile.builder()
                                .user(candidate)
                                .fullName("Báo cáo " + System.currentTimeMillis())
                                .phone("0900000099")
                                .build();

                candidate.setProfile(profile);
                userRepository.save(candidate);

                return reportRepository.save(Report.builder()
                                .job(job)
                                .reportedBy(candidate)
                                .reason(reason)
                                .details("Test report details")
                                .status(ReportStatus.PENDING)
                                .build());
        }

        @Test
        void admin_canListReports() throws Exception {
                Company company = createPendingCompany("Reports Corp", "131313131", "reports@test.com", "0900000013");
                Job job1 = createPendingJob(company, "Reported Job 1");
                Job job2 = createPendingJob(company, "Reported Job 2");
                createReport(job1, ReportReason.SPAM);
                createReport(job2, ReportReason.SCAM);

                Cookie adminCookie = loginAsAdmin();

                MvcResult result = mockMvc.perform(get("/api/admin/reports")
                                .cookie(adminCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(json.get("content").isArray()).isTrue();
                assertThat(json.get("totalElements").asInt()).isEqualTo(2);
        }

        @Test
        void admin_canFilterReportsByStatus() throws Exception {
                Company company = createPendingCompany("Filter Rp Corp", "141414141", "filterrp@test.com",
                                "0900000014");
                Job job = createPendingJob(company, "Filterable Job");
                createReport(job, ReportReason.SPAM);

                Cookie adminCookie = loginAsAdmin();

                MvcResult result = mockMvc.perform(get("/api/admin/reports")
                                .cookie(adminCookie)
                                .param("status", "PENDING"))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("status").asText()).isEqualTo("PENDING");
        }

        @Test
        void listReports_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/admin/reports"))
                                .andExpect(status().isUnauthorized());
        }

        // ----------------------------------------------------------------
        // Admin Reports — PATCH /api/admin/reports/{id}/review
        // ----------------------------------------------------------------

        @Test
        void admin_canReviewReport() throws Exception {
                Company company = createPendingCompany("Review Rp Corp", "151515151", "reviewrp@test.com",
                                "0900000015");
                Job job = createPendingJob(company, "Reviewable Job");
                Report report = createReport(job, ReportReason.INAPPROPRIATE);

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/reports/" + report.getId() + "/review")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Duyệt báo cáo thành công"));

                assertThat(reportRepository.findById(report.getId()).orElseThrow().getStatus())
                                .isEqualTo(ReportStatus.REVIEWED);
        }

        @Test
        void admin_canDismissReport() throws Exception {
                Company company = createPendingCompany("Dismiss Rp Corp", "161616161", "dismissrp@test.com",
                                "0900000016");
                Job job = createPendingJob(company, "Dismissable Job");
                Report report = createReport(job, ReportReason.SPAM);

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/reports/" + report.getId() + "/dismiss")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Gỡ bỏ báo cáo thành công"));

                assertThat(reportRepository.findById(report.getId()).orElseThrow().getStatus())
                                .isEqualTo(ReportStatus.DISMISSED);
        }

        @Test
        void admin_canResolveReport() throws Exception {
                Company company = createPendingCompany("Resolve Rp Corp", "171717171", "resolverp@test.com",
                                "0900000017");
                Job job = createPendingJob(company, "Resolvable Job");
                Report report = createReport(job, ReportReason.SCAM);
                report.setStatus(ReportStatus.REVIEWED);
                report = reportRepository.save(report);

                Cookie adminCookie = loginAsAdmin();

                mockMvc.perform(patch("/api/admin/reports/" + report.getId() + "/resolve")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Giải quyết báo cáo thành công"));

                assertThat(reportRepository.findById(report.getId()).orElseThrow().getStatus())
                                .isEqualTo(ReportStatus.RESOLVED);
        }

        @Test
        void admin_reviewReport_withNotes() throws Exception {
                Company company = createPendingCompany("Notes Rp Corp", "181818181", "notesrp@test.com", "0900000018");
                Job job = createPendingJob(company, "Notes Job");
                Report report = createReport(job, ReportReason.OTHER);

                Cookie adminCookie = loginAsAdmin();

                String payload = objectMapper.writeValueAsString(Map.of(
                                "reviewNotes", "Đã xem xét, cần theo dõi thêm"));

                mockMvc.perform(patch("/api/admin/reports/" + report.getId() + "/review")
                                .cookie(adminCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk());

                Report updated = reportRepository.findById(report.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(ReportStatus.REVIEWED);
                assertThat(updated.getReviewNotes()).isEqualTo("Đã xem xét, cần theo dõi thêm");
        }

        @Test
        void admin_canGetDashboardStats() throws Exception {
        createPendingCompany(
                        "Pending Dashboard Corp",
                        "123456789",
                        "pending-dashboard@example.com",
                        "0900000011");

        Company approvedCompany = createPendingCompany(
                        "Approved Dashboard Corp",
                        "987654321",
                        "approved-dashboard@example.com",
                        "0900000012");

                approvedCompany.setStatus(CompanyStatus.APPROVED);
                approvedCompany.setApproved(true);
                approvedCompany = companyRepository.save(approvedCompany);

                Job activeJob = createPendingJob(approvedCompany, "Dashboard Active Job");
                activeJob.setStatus(JobStatus.ACTIVE);
                activeJob = jobRepository.save(activeJob);

                createPendingJob(approvedCompany, "Dashboard Pending Job");

        Profile candidateProfile = createCandidateProfile("candidate-dashboard@example.com");

        applicationRepository.save(Application.builder()
                        .candidate(candidateProfile)
                        .job(activeJob)
                        .status(ApplicationStatus.PENDING)
                        .coverLetter("Tôi muốn ứng tuyển")
                        .appliedAt(OffsetDateTime.now())
                        .build());

        Cookie adminCookie = loginAsAdmin();

        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .cookie(adminCookie))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalUsers").value(4))
                        .andExpect(jsonPath("$.totalCompanies").value(2))
                        .andExpect(jsonPath("$.totalJobs").value(1))
                        .andExpect(jsonPath("$.totalApplications").value(1))
                        .andExpect(jsonPath("$.newUsers").value(4))
                        .andExpect(jsonPath("$.pendingJobs").value(1))
                        .andExpect(jsonPath("$.pendingCompanies").value(1));
        }
}
