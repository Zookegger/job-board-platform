package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class EmployerJobControllerTest {

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
        PasswordEncoder passwordEncoder;

        private final ObjectMapper objectMapper = new ObjectMapper();
        private JobCategory savedCategory;
        private Skill savedSkill;

        @BeforeEach
        void cleanup() {
                candidateDetailRepository.deleteAll();
                jobSkillRepository.deleteAll();
                jobRepository.deleteAll();
                jobCategoryRepository.deleteAll();
                skillRepository.deleteAll();
                companyEmployerDetailRepository.deleteAll();
                companyRepository.deleteAll();
                profileRepository.deleteAll();
                userRepository.deleteAll();

                savedCategory = jobCategoryRepository.save(
                                JobCategory.builder().name("IT").build());
                savedSkill = skillRepository.save(
                                Skill.builder().name("Java").build());
        }

        private Cookie registerAndLogin(String email, String password) throws Exception {
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

    private String createJobPayload(String title) throws Exception {
        return objectMapper.writeValueAsString(Map.ofEntries(
                Map.entry("title", title),
                Map.entry("description", "Job description"),
                Map.entry("categoryId", savedCategory.getId()),
                Map.entry("locationTypes", "ONSITE"),
                Map.entry("employmentType", "FULL_TIME"),
                Map.entry("experienceLevel", "MID"),
                Map.entry("numberOfOpenings", 2),
                Map.entry("salaryMin", 10000000),
                Map.entry("salaryMax", 20000000),
                Map.entry("currency", "VND"),
                Map.entry("location", "Hồ Chí Minh"),
                Map.entry("skillIds", List.of(savedSkill.getId()))));
    }

        // -----------------------------------------------------------------
        // POST /api/employer/jobs — createJob
        // -----------------------------------------------------------------

        @Test
        void employer_createJob_success() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-create@test.com", "password123");

                MvcResult res = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Software Engineer")))
                                .andExpect(status().isCreated())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("Software Engineer");
                assertThat(json.get("status").asText()).isEqualTo("DRAFT");
                assertThat(json.get("slug")).isNotNull();
                assertThat(json.get("categoryId").asInt()).isEqualTo(savedCategory.getId());
        }

        @Test
        void createJob_withoutAuth_returns401() throws Exception {
                mockMvc.perform(post("/api/employer/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("No Auth Job")))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void createJob_asCandidate_returns403() throws Exception {
                Cookie tokenCookie = registerAndLoginCandidate("candidate-job@test.com", "password123");

                mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Candidate Job")))
                                .andExpect(status().isForbidden());
        }

        // -----------------------------------------------------------------
        // GET /api/employer/jobs — getEmployerJobs
        // -----------------------------------------------------------------

        @Test
        void employer_listJobs_noFilter_returnsJobs() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-list@test.com", "password123");

                mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Job One")))
                                .andExpect(status().isCreated());

                MvcResult res = mockMvc.perform(get("/api/employer/jobs")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("totalElements").asInt()).isEqualTo(1);
                assertThat(json.get("content").get(0).get("title").asText()).isEqualTo("Job One");
        }

        @Test
        void employer_listJobs_filterByStatus_returnsFiltered() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-filter@test.com", "password123");

                // Create a draft job
                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Draft Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                // Submit it → PENDING_APPROVAL
                mockMvc.perform(post("/api/employer/jobs/" + jobId + "/submit")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk());

                // Filter by DRAFT should return empty
                MvcResult draftRes = mockMvc.perform(get("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .param("status", "DRAFT"))
                                .andExpect(status().isOk())
                                .andReturn();
                var draftJson = objectMapper.readTree(draftRes.getResponse().getContentAsString());
                assertThat(draftJson.get("totalElements").asInt()).isEqualTo(0);

                // Filter by PENDING_APPROVAL should return the job
                MvcResult pendingRes = mockMvc.perform(get("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .param("status", "PENDING_APPROVAL"))
                                .andExpect(status().isOk())
                                .andReturn();
                var pendingJson = objectMapper.readTree(pendingRes.getResponse().getContentAsString());
                assertThat(pendingJson.get("totalElements").asInt()).isEqualTo(1);
        }

        @Test
        void listJobs_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/api/employer/jobs"))
                                .andExpect(status().isUnauthorized());
        }

        // -----------------------------------------------------------------
        // GET /api/employer/jobs/{id} — getJobDetail
        // -----------------------------------------------------------------

        @Test
        void employer_getOwnJobDetail_success() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-detail@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Detail Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                MvcResult res = mockMvc.perform(get("/api/employer/jobs/" + jobId)
                                .cookie(tokenCookie))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(res.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("Detail Job");
                assertThat(json.get("categoryId").asInt()).isEqualTo(savedCategory.getId());
                assertThat(json.get("status").asText()).isEqualTo("DRAFT");
        }

        @Test
        void employer_getAnotherEmployersJob_returns403() throws Exception {
                Cookie employerA = registerAndLogin("employer-a@test.com", "password123");
                Cookie employerB = registerAndLogin("employer-b@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(employerA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Job A")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                mockMvc.perform(get("/api/employer/jobs/" + jobId)
                                .cookie(employerB))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getJobDetail_notFound_returns404() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-notfound@test.com", "password123");

                mockMvc.perform(get("/api/employer/jobs/" + UUID.randomUUID())
                                .cookie(tokenCookie))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getJobDetail_withoutAuth_returns401() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-get@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Get Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                mockMvc.perform(get("/api/employer/jobs/" + jobId))
                                .andExpect(status().isUnauthorized());
        }

        // -----------------------------------------------------------------
        // PUT /api/employer/jobs/{id} — updateJob
        // -----------------------------------------------------------------

        @Test
        void employer_updateDraftJob_keepsDraft() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-update-draft@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Draft Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                var updatePayload = objectMapper.writeValueAsString(Map.of(
                                "title", "Updated Draft Job",
                                "description", "Updated description",
                                "categoryId", savedCategory.getId(),
                                "locationTypes", "ONSITE",
                                "employmentType", "FULL_TIME",
                                "experienceLevel", "MID"));

                MvcResult updateRes = mockMvc.perform(put("/api/employer/jobs/" + jobId)
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePayload))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(updateRes.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("Updated Draft Job");
                assertThat(json.get("status").asText()).isEqualTo("DRAFT");
        }

        @Test
        void employer_updateActiveJob_resetsToDraft() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-update-active@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Active Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                // Manually approve to ACTIVE
                Job job = jobRepository.findById(jobId).orElseThrow();
                job.setStatus(JobStatus.ACTIVE);
                jobRepository.save(job);

                var updatePayload = objectMapper.writeValueAsString(Map.of(
                                "title", "Edited Active Job",
                                "description", "Updated description",
                                "categoryId", savedCategory.getId(),
                                "locationTypes", "ONSITE",
                                "employmentType", "FULL_TIME",
                                "experienceLevel", "MID"));

                MvcResult updateRes = mockMvc.perform(put("/api/employer/jobs/" + jobId)
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePayload))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(updateRes.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("Edited Active Job");
                assertThat(json.get("status").asText()).isEqualTo("DRAFT");
        }

        @Test
        void employer_updatePendingApprovalJob_resetsToDraft() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-update-pending@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Pending Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                // Submit for review → PENDING_APPROVAL
                mockMvc.perform(post("/api/employer/jobs/" + jobId + "/submit")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk());

                var updatePayload = objectMapper.writeValueAsString(Map.of(
                                "title", "Edited Pending Job",
                                "description", "Updated description",
                                "categoryId", savedCategory.getId(),
                                "locationTypes", "ONSITE",
                                "employmentType", "FULL_TIME",
                                "experienceLevel", "MID"));

                MvcResult updateRes = mockMvc.perform(put("/api/employer/jobs/" + jobId)
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePayload))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(updateRes.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("Edited Pending Job");
                assertThat(json.get("status").asText()).isEqualTo("DRAFT");
        }

        @Test
        void employer_updateRejectedJob_resetsToDraft() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-update-rejected@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Rejected Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                // Manually set to REJECTED
                Job job = jobRepository.findById(jobId).orElseThrow();
                job.setStatus(JobStatus.REJECTED);
                jobRepository.save(job);

                var updatePayload = objectMapper.writeValueAsString(Map.of(
                                "title", "Edited Rejected Job",
                                "description", "Updated description",
                                "categoryId", savedCategory.getId(),
                                "locationTypes", "ONSITE",
                                "employmentType", "FULL_TIME",
                                "experienceLevel", "MID"));

                MvcResult updateRes = mockMvc.perform(put("/api/employer/jobs/" + jobId)
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePayload))
                                .andExpect(status().isOk())
                                .andReturn();

                var json = objectMapper.readTree(updateRes.getResponse().getContentAsString());
                assertThat(json.get("title").asText()).isEqualTo("Edited Rejected Job");
                assertThat(json.get("status").asText()).isEqualTo("DRAFT");
        }

        @Test
        void employer_updateAnotherEmployersJob_returns403() throws Exception {
                Cookie employerA = registerAndLogin("employer-update-a@test.com", "password123");
                Cookie employerB = registerAndLogin("employer-update-b@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(employerA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Job A")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                var updatePayload = objectMapper.writeValueAsString(Map.of(
                                "title", "Hacked Job",
                                "description", "Updated description",
                                "categoryId", savedCategory.getId(),
                                "locationTypes", "ONSITE",
                                "employmentType", "FULL_TIME",
                                "experienceLevel", "MID"));

                mockMvc.perform(put("/api/employer/jobs/" + jobId)
                                .cookie(employerB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePayload))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateJob_withoutAuth_returns401() throws Exception {
                var updatePayload = objectMapper.writeValueAsString(Map.of(
                                "title", "No Auth",
                                "description", "Updated description",
                                "categoryId", savedCategory.getId(),
                                "locationTypes", "ONSITE",
                                "employmentType", "FULL_TIME",
                                "experienceLevel", "MID"));

                mockMvc.perform(put("/api/employer/jobs/" + UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePayload))
                                .andExpect(status().isUnauthorized());
        }

        // -----------------------------------------------------------------
        // POST /api/employer/jobs/{id}/submit — submitForReview
        // -----------------------------------------------------------------

        @Test
        void employer_submitDraftJob_success() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-submit@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Submit Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                mockMvc.perform(post("/api/employer/jobs/" + jobId + "/submit")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Gửi duyệt thành công"));

                Job updated = jobRepository.findById(jobId).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(JobStatus.PENDING_APPROVAL);
        }

        @Test
        void employer_submitPendingApprovalJob_returns400() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-submit-pending@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Already Pending")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                // Submit once → PENDING_APPROVAL
                mockMvc.perform(post("/api/employer/jobs/" + jobId + "/submit")
                                .cookie(tokenCookie))
                                .andExpect(status().isOk());

                // Submit again → 400
                mockMvc.perform(post("/api/employer/jobs/" + jobId + "/submit")
                                .cookie(tokenCookie))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Chỉ có thể gửi duyệt tin ở trạng thái DRAFT"));
        }

        @Test
        void submitForReview_withoutAuth_returns401() throws Exception {
                mockMvc.perform(post("/api/employer/jobs/" + UUID.randomUUID() + "/submit"))
                                .andExpect(status().isUnauthorized());
        }

        // -----------------------------------------------------------------
        // DELETE /api/employer/jobs/{id} — deleteJob
        // -----------------------------------------------------------------

        @Test
        void employer_deleteOwnJob_success() throws Exception {
                Cookie tokenCookie = registerAndLogin("employer-delete@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(tokenCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Delete Job")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                mockMvc.perform(delete("/api/employer/jobs/" + jobId)
                                .cookie(tokenCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Xóa tin tuyển dụng thành công"));

                assertThat(jobRepository.findById(jobId)).isEmpty();
        }

        @Test
        void employer_deleteAnotherEmployersJob_returns403() throws Exception {
                Cookie employerA = registerAndLogin("employer-del-a@test.com", "password123");
                Cookie employerB = registerAndLogin("employer-del-b@test.com", "password123");

                MvcResult createRes = mockMvc.perform(post("/api/employer/jobs")
                                .cookie(employerA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJobPayload("Job A")))
                                .andExpect(status().isCreated())
                                .andReturn();
                var createdJson = objectMapper.readTree(createRes.getResponse().getContentAsString());
                UUID jobId = UUID.fromString(createdJson.get("id").asText());

                mockMvc.perform(delete("/api/employer/jobs/" + jobId)
                                .cookie(employerB))
                                .andExpect(status().isForbidden());
        }

        @Test
        void deleteJob_withoutAuth_returns401() throws Exception {
                mockMvc.perform(delete("/api/employer/jobs/" + UUID.randomUUID()))
                                .andExpect(status().isUnauthorized());
        }
}
