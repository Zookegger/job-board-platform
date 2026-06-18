package com.yoedu.job_board_platform.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.LocationTypes;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CompanyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CompanyEmployerDetailRepository companyEmployerDetailRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired JobRepository jobRepository;
    @Autowired JobCategoryRepository jobCategoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        jobRepository.deleteAll();
        companyEmployerDetailRepository.deleteAll();
        companyRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Cookie registerAndLogin(String email, String password, boolean isCandidate) throws Exception {
        return registerAndLogin(email, password, isCandidate, "Test Corp");
    }

    private Cookie registerAndLogin(String email, String password, boolean isCandidate, String companyName)
            throws Exception {
        if (isCandidate) {
            var registerPayload = objectMapper.writeValueAsString(java.util.Map.of(
                    "email", email,
                    "fullName", "Nguyễn Văn A",
                    "password", password,
                    "confirmPassword", password));
            mockMvc.perform(post("/api/auth/register/candidate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerPayload))
                    .andExpect(status().isCreated());
        } else {
            var registerPayload = objectMapper.writeValueAsString(java.util.Map.of(
                    "companyName", companyName,
                    "taxCode", "0123456789",
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
        }

        var loginPayload = objectMapper.writeValueAsString(
                java.util.Map.of("email", email, "password", password));
        MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return loginRes.getResponse().getCookie("accessToken");
    }

    // -----------------------------------------------------------------
    // GET /api/company/employer
    // -----------------------------------------------------------------

    @Test
    void employer_getMyCompany_success() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-view@test.com", "password123", false);

        MvcResult res = mockMvc.perform(get("/api/company/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("companyName").asText()).isEqualTo("Test Corp");
        assertThat(json.get("address").asText()).isEqualTo("123 Street");
        assertThat(json.get("slug")).isNotNull();
        assertThat(json.get("taxCode").asText()).isEqualTo("0123456789");
        assertThat(json.get("status").asText()).isEqualTo("PENDING");
        assertThat(json.get("isApproved").asBoolean()).isFalse();
        assertThat(json.get("rejectionReason")).isNull();
        assertThat(json.get("reviewReason").asText()).isEqualTo("NEW_COMPANY");
    }

    @Test
    void candidate_getMyCompany_returns400() throws Exception {
        Cookie tokenCookie = registerAndLogin("candidate-view@test.com", "password123", true);

        mockMvc.perform(get("/api/company/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Người dùng không phải nhà tuyển dụng"));
    }

    @Test
    void getMyCompany_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/company/employer"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------
    // PUT /api/company
    // -----------------------------------------------------------------

    @Test
    void employer_updateCompany_updatesFields() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-update@test.com", "password123", false);

        var updatePayload = objectMapper.writeValueAsString(java.util.Map.of(
                "companyName", "Updated Corp",
                "address", "456 New Street",
                "description", "A great company",
                "website", "https://updated.com",
                "email", "info@updated.com",
                "phone", "0988777666"));

        MvcResult res = mockMvc.perform(put("/api/company")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("companyName").asText()).isEqualTo("Updated Corp");
        assertThat(json.get("address").asText()).isEqualTo("456 New Street");
        assertThat(json.get("description").asText()).isEqualTo("A great company");
        assertThat(json.get("website").asText()).isEqualTo("https://updated.com");
        assertThat(json.get("email").asText()).isEqualTo("info@updated.com");
        assertThat(json.get("phone").asText()).isEqualTo("0988777666");
    }

    @Test
    void employer_updateCompany_partialUpdate_keepsExisting() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-partial@test.com", "password123", false);

        var firstUpdate = objectMapper.writeValueAsString(java.util.Map.of(
                "companyName", "Partial Corp",
                "address", "789 Partial Street",
                "description", "Partial description",
                "website", "https://partial.com"));

        mockMvc.perform(put("/api/company")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstUpdate))
                .andExpect(status().isOk());

        var secondUpdate = objectMapper.writeValueAsString(
                java.util.Map.of("companyName", "Only Name Changed"));

        mockMvc.perform(put("/api/company")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondUpdate))
                .andExpect(status().isOk());

        MvcResult res = mockMvc.perform(get("/api/company/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("companyName").asText()).isEqualTo("Only Name Changed");
        assertThat(json.get("address").asText()).isEqualTo("789 Partial Street");
        assertThat(json.get("website").asText()).isEqualTo("https://partial.com");
        assertThat(json.get("description").asText()).isEqualTo("Partial description");
    }

    @Test
    void employer_updateCompany_approvedCompanyTriggersReview() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-review@test.com", "password123", false);

        MvcResult companyRes = mockMvc.perform(get("/api/company/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();
        var companyJson = objectMapper.readTree(companyRes.getResponse().getContentAsString());
        UUID companyId = UUID.fromString(companyJson.get("id").asText());

        var company = companyRepository.findById(companyId).orElseThrow();
        company.setStatus(CompanyStatus.APPROVED);
        company.setApproved(true);
        companyRepository.save(company);

        var updatePayload = objectMapper.writeValueAsString(
                java.util.Map.of("companyName", "Review Trigger Corp"));

        MvcResult res = mockMvc.perform(put("/api/company")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("companyName").asText()).isEqualTo("Review Trigger Corp");
        assertThat(json.get("status").asText()).isEqualTo("PENDING");
        assertThat(json.get("isApproved").asBoolean()).isFalse();
        assertThat(json.get("rejectionReason")).isNull();
        assertThat(json.get("reviewReason").asText()).isEqualTo("INFO_UPDATED");
    }

    @Test
    void candidate_updateCompany_returns403() throws Exception {
        Cookie tokenCookie = registerAndLogin("candidate-update@test.com", "password123", true);

        var payload = objectMapper.writeValueAsString(
                java.util.Map.of("companyName", "Hack Corp"));

        mockMvc.perform(put("/api/company")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCompany_withoutAuth_returns401() throws Exception {
        var payload = objectMapper.writeValueAsString(
                java.util.Map.of("companyName", "Hack Corp"));

        mockMvc.perform(put("/api/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------
    // GET /api/company/job-post
    // -----------------------------------------------------------------

    @Test
    void getCompanyByJobPost_success() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-jobpost@test.com", "password123", false);

        MvcResult companyRes = mockMvc.perform(get("/api/company/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();
        var companyJson = objectMapper.readTree(companyRes.getResponse().getContentAsString());
        UUID companyId = UUID.fromString(companyJson.get("id").asText());

        var company = companyRepository.findById(companyId).orElseThrow();
        var category = jobCategoryRepository.save(
                JobCategory.builder().name("Test Cat").build());
        var job = jobRepository.save(Job.builder()
                .company(company)
                .category(category)
                .title("Software Engineer")
                .slug("se-" + UUID.randomUUID())
                .description("Build great software")
                .locationTypes(LocationTypes.ONSITE)
                .employmentType(EmploymentType.FULL_TIME)
                .experienceLevel(ExperienceLevel.MID)
                .build());

        MvcResult res = mockMvc.perform(get("/api/company/job-post")
                        .param("jobPostId", job.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("companyName").asText()).isEqualTo("Test Corp");
    }

    @Test
    void getCompanyByJobPost_notFound() throws Exception {
        mockMvc.perform(get("/api/company/job-post")
                        .param("jobPostId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------
    // GET /api/company
    // -----------------------------------------------------------------

    @Test
    void listCompanies_returnsAll() throws Exception {
        registerAndLogin("employer-list1@test.com", "password123", false, "Corp A");
        registerAndLogin("employer-list2@test.com", "password123", false, "Corp B");

        MvcResult res = mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json).hasSize(2);
    }

    @Test
    void listCompanies_emptyWhenNoCompanies() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json).isEmpty();
    }
}
