package com.yoedu.job_board_platform.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.ApplicationStatusLogRepository;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.ProfileRepository;
import com.yoedu.job_board_platform.repositories.RefreshTokenRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SkillControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    ApplicationStatusLogRepository applicationStatusLogRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    CandidateSkillRepository candidateSkillRepository;

    @Autowired
    JobSkillRepository jobSkillRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Skill activeSkill;
    private Skill inactiveSkill;

    @BeforeEach
    void cleanup() {
        applicationStatusLogRepository.deleteAll();
        applicationRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        candidateSkillRepository.deleteAll();
        jobSkillRepository.deleteAll();
        skillRepository.deleteAll();
        profileRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        activeSkill = skillRepository.save(
                Skill.builder().name("Java").isActive(true).build());
        inactiveSkill = skillRepository.save(
                Skill.builder().name("Kotlin").isActive(false).build());
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

    // ----------------------------------------------------------------
    // GET /api/skills — public, active only
    // ----------------------------------------------------------------

    @Test
    void getAllSkills_public_returnsActiveOnly() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Java"))
                .andExpect(jsonPath("$.content[0].isActive").value(true))
                .andExpect(jsonPath("$.content[*].id").value(not(hasItem(inactiveSkill.getId()))))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllSkills_emptyWhenNoSkills() throws Exception {
        skillRepository.deleteAll();

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ----------------------------------------------------------------
    // GET /api/skills/profile — candidate only
    // ----------------------------------------------------------------

    @Test
    void getCandidateSkills_withCandidateAuth_returnsSkills() throws Exception {
        Cookie cookie = registerAndLogin("candidate-skills@test.com", "password123");

        mockMvc.perform(get("/api/skills/profile").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // empty initially
    }

    @Test
	void getCandidateSkills_withoutAuth_returns403() throws Exception {
		mockMvc.perform(get("/api/skills/profile"))
				.andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------
    // PUT /api/skills/profile — candidate only
    // ----------------------------------------------------------------

    @Test
    void updateCandidateSkills_withValidData_returnsUpdated() throws Exception {
        Cookie cookie = registerAndLogin("candidate-update@test.com", "password123");

        var payload = objectMapper.writeValueAsString(Map.of(
                "skills", List.of(Map.of("skillId", activeSkill.getId(), "proficientLevel", "INTERMEDIATE"))));

        mockMvc.perform(put("/api/skills/profile")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].skillId").value(activeSkill.getId()))
                .andExpect(jsonPath("$[0].proficientLevel").value("INTERMEDIATE"));
    }

    @Test
    void updateCandidateSkills_withInvalidSkillId_returns400() throws Exception {
        Cookie cookie = registerAndLogin("candidate-invalid-skill@test.com", "password123");

        var payload = objectMapper.writeValueAsString(Map.of(
                "skills", List.of(Map.of("skillId", 999, "proficientLevel", "BEGINNER"))));

        mockMvc.perform(put("/api/skills/profile")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
	void updateCandidateSkills_withoutAuth_returns403() throws Exception {
		var payload = objectMapper.writeValueAsString(Map.of(
				"skills", List.of(Map.of("skillId", 1, "proficientLevel", "BEGINNER"))));

		mockMvc.perform(put("/api/skills/profile")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isForbidden());
    }
}
