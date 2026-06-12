package com.yoedu.job_board_platform.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.ResumeRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CompanyEmployerDetailRepository companyEmployerDetailRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired JobRepository jobRepository;
    @Autowired ResumeRepository resumeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        jobRepository.deleteAll();
        companyEmployerDetailRepository.deleteAll();
        companyRepository.deleteAll();
        resumeRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Cookie registerAndLogin(String email, String password, boolean isCandidate) throws Exception {
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
                    "companyName", "Test Corp",
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
    // Candidate profile update
    // -----------------------------------------------------------------

    @Test
    void candidate_updateProfile_updatesFieldsInDatabase() throws Exception {
        Cookie tokenCookie = registerAndLogin("candidate-update@test.com", "password123", true);

        var updatePayload = new java.util.HashMap<String, Object>();
        updatePayload.put("fullName", "Nguyễn Văn B");
        updatePayload.put("phone", "0999888777");

        MvcResult updateRes = mockMvc.perform(put("/api/profile/candidate")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andReturn();

        var updatedJson = objectMapper.readTree(updateRes.getResponse().getContentAsString());
        assertThat(updatedJson.get("fullName").asText()).isEqualTo("Nguyễn Văn B");
        assertThat(updatedJson.get("phone").asText()).isEqualTo("0999888777");

        MvcResult getRes = mockMvc.perform(get("/api/profile/candidate")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        var getJson = objectMapper.readTree(getRes.getResponse().getContentAsString());
        assertThat(getJson.get("fullName").asText()).isEqualTo("Nguyễn Văn B");
        assertThat(getJson.get("phone").asText()).isEqualTo("0999888777");
    }

    @Test
    void candidate_updateProfile_withNullFields_keepsExistingValues() throws Exception {
        Cookie tokenCookie = registerAndLogin("candidate-partial@test.com", "password123", true);

        var firstUpdate = new java.util.HashMap<String, Object>();
        firstUpdate.put("fullName", "Nguyễn Văn C");
        firstUpdate.put("phone", "0912345678");
        firstUpdate.put("avatarUrl", "https://example.com/avatar.jpg");

        mockMvc.perform(put("/api/profile/candidate")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isOk());

        var secondUpdate = new java.util.HashMap<String, Object>();
        secondUpdate.put("fullName", "Nguyễn Văn D");

        mockMvc.perform(put("/api/profile/candidate")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUpdate)))
                .andExpect(status().isOk());

        MvcResult res = mockMvc.perform(get("/api/profile/candidate")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("fullName").asText()).isEqualTo("Nguyễn Văn D");
        assertThat(json.get("phone").asText()).isEqualTo("0912345678");
        assertThat(json.get("avatarUrl").asText()).isEqualTo("https://example.com/avatar.jpg");
    }

    // -----------------------------------------------------------------
    // Employer profile update
    // -----------------------------------------------------------------

    @Test
    void employer_updateProfile_updatesFieldsInDatabase() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-update@test.com", "password123", false);

        var updatePayload = new java.util.HashMap<String, Object>();
        updatePayload.put("fullName", "Trần Thị C");
        updatePayload.put("phone", "0900111222");
        updatePayload.put("avatarUrl", "https://example.com/employer-avatar.jpg");

        MvcResult res = mockMvc.perform(put("/api/profile/employer")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("fullName").asText()).isEqualTo("Trần Thị C");
        assertThat(json.get("phone").asText()).isEqualTo("0900111222");
        assertThat(json.get("avatarUrl").asText()).isEqualTo("https://example.com/employer-avatar.jpg");
    }

    // -----------------------------------------------------------------
    // Security & edge cases
    // -----------------------------------------------------------------

    @Test
    void updateProfile_withoutAuth_returns401() throws Exception {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("fullName", "Hacker");

        mockMvc.perform(put("/api/profile/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidate_getCandidateProfile_returnsRoleSpecificData() throws Exception {
        Cookie tokenCookie = registerAndLogin("candidate-specific@test.com", "password123", true);

        mockMvc.perform(get("/api/profile/candidate")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.role").value("CANDIDATE"))
                .andExpect(jsonPath("$.cvFileUrl").doesNotExist());
    }

    @Test
    void employer_getEmployerProfile_returnsRoleSpecificData() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-specific@test.com", "password123", false);

        mockMvc.perform(get("/api/profile/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Trần Thị B"))
                .andExpect(jsonPath("$.role").value("EMPLOYER"))
                .andExpect(jsonPath("$.companyName").value("Test Corp"))
                .andExpect(jsonPath("$.roleInCompany").value("HR"));
    }

    @Test
    void candidate_getEmployerProfile_returns403() throws Exception {
        Cookie tokenCookie = registerAndLogin("candidate-no-employer@test.com", "password123", true);

        mockMvc.perform(get("/api/profile/employer")
                        .cookie(tokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void employer_getCandidateProfile_returns403() throws Exception {
        Cookie tokenCookie = registerAndLogin("employer-no-candidate@test.com", "password123", false);

        mockMvc.perform(get("/api/profile/candidate")
                        .cookie(tokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_withEmptyBody_doesNotChangeData() throws Exception {
        Cookie tokenCookie = registerAndLogin("empty-update@test.com", "password123", true);

        var emptyPayload = new java.util.HashMap<String, Object>();

        mockMvc.perform(put("/api/profile/candidate")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"));
    }

    // -----------------------------------------------------------------
    // Resume (CV) endpoints
    // -----------------------------------------------------------------

    @Test
    void resume_uploadAndDownload_success() throws Exception {
        Cookie tokenCookie = registerAndLogin("resume-test@test.com", "password123", true);

        var pdfFile = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.4 fake pdf content".getBytes());

        MvcResult uploadRes = mockMvc.perform(multipart("/api/profile/resume")
                        .file(pdfFile)
                        .param("title", "Nguyễn Văn A - Software Engineer")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        var uploadJson = objectMapper.readTree(uploadRes.getResponse().getContentAsString());
        assertThat(uploadJson.get("title").asText()).isEqualTo("Nguyễn Văn A - Software Engineer");
        assertThat(uploadJson.get("originalFileName").asText()).isEqualTo("cv.pdf");
        assertThat(uploadJson.get("fileType").asText()).isEqualTo("application/pdf");
        assertThat(uploadJson.get("fileSize").asLong()).isGreaterThan(0);
        assertThat(uploadJson.get("id")).isNotNull();

        MvcResult getRes = mockMvc.perform(get("/api/profile/resume")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andReturn();

        var getJson = objectMapper.readTree(getRes.getResponse().getContentAsString());
        assertThat(getJson.get("title").asText()).isEqualTo("Nguyễn Văn A - Software Engineer");

        mockMvc.perform(get("/api/profile/resume/download")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    var contentType = result.getResponse().getContentType();
                    assertThat(contentType).isEqualTo("application/pdf");
                });
    }

    @Test
    void resume_uploadWithoutResume_returns404() throws Exception {
        Cookie tokenCookie = registerAndLogin("resume-nocv@test.com", "password123", true);

        mockMvc.perform(get("/api/profile/resume")
                        .cookie(tokenCookie))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/profile/resume/download")
                        .cookie(tokenCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void resume_uploadInvalidPdf_returns400() throws Exception {
        Cookie tokenCookie = registerAndLogin("resume-invalid@test.com", "password123", true);

        var textFile = new MockMultipartFile("file", "resume.txt", "text/plain", "not a pdf".getBytes());

        mockMvc.perform(multipart("/api/profile/resume")
                        .file(textFile)
                        .cookie(tokenCookie))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resume_updateMetadata_success() throws Exception {
        Cookie tokenCookie = registerAndLogin("resume-meta@test.com", "password123", true);

        var pdfFile = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.4 fake content".getBytes());
        mockMvc.perform(multipart("/api/profile/resume")
                        .file(pdfFile)
                        .param("title", "Original Title")
                        .cookie(tokenCookie))
                .andExpect(status().isOk());

        var updatePayload = objectMapper.writeValueAsString(java.util.Map.of("title", "Updated Title"));
        mockMvc.perform(put("/api/profile/resume")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void resume_deleteRemovesFileAndRecord() throws Exception {
        Cookie tokenCookie = registerAndLogin("resume-delete@test.com", "password123", true);

        var pdfFile = new MockMultipartFile("file", "cv.pdf", "application/pdf", "%PDF-1.4 fake content".getBytes());
        mockMvc.perform(multipart("/api/profile/resume")
                        .file(pdfFile)
                        .cookie(tokenCookie))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/profile/resume")
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/profile/resume")
                        .cookie(tokenCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void resume_uploadReplacesOldFile() throws Exception {
        Cookie tokenCookie = registerAndLogin("resume-replace@test.com", "password123", true);

        var firstFile = new MockMultipartFile("file", "v1.pdf", "application/pdf", "%PDF-1.4 first version".getBytes());
        mockMvc.perform(multipart("/api/profile/resume")
                        .file(firstFile)
                        .param("title", "Version 1")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("v1.pdf"));

        var secondFile = new MockMultipartFile("file", "v2.pdf", "application/pdf", "%PDF-1.4 second version longer content".getBytes());
        mockMvc.perform(multipart("/api/profile/resume")
                        .file(secondFile)
                        .param("title", "Version 2")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("v2.pdf"))
                .andExpect(jsonPath("$.title").value("Version 2"));
    }
}
