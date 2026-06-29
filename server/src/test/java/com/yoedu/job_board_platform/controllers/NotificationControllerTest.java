package com.yoedu.job_board_platform.controllers;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.utils.DatabaseCleaner;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        DatabaseCleaner.cleanAllTables(jdbcTemplate, entityManager);
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

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

    private Notification createNotification(String email, boolean isRead) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Notification n = Notification.builder()
                .user(user)
                .type(NotificationStatus.APPLICATION_STATUS_CHANGED)
                .entityId(UUID.randomUUID())
                .message("Đơn ứng tuyển của bạn đã được xem xét")
                .build();
        if (isRead) {
            n.setReadAt(OffsetDateTime.now());
        }
        return notificationRepository.save(n);
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/notifications/unread-count
    // ─────────────────────────────────────────────────────────────

    @Test
    void getUnreadCount_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUnreadCount_noNotifications_returns0() throws Exception {
        Cookie token = registerAndLoginCandidate("count-empty@test.com", "password123");

        mockMvc.perform(get("/api/notifications/unread-count").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    void getUnreadCount_withUnreadNotifications_returnsCorrectCount() throws Exception {
        Cookie token = registerAndLoginCandidate("count-unread@test.com", "password123");

        createNotification("count-unread@test.com", false);
        createNotification("count-unread@test.com", false);
        createNotification("count-unread@test.com", true); // đã đọc, không tính

        mockMvc.perform(get("/api/notifications/unread-count").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    void getUnreadCount_onlyCountsCurrentUser() throws Exception {
        Cookie tokenA = registerAndLoginCandidate("count-userA@test.com", "password123");
        registerAndLoginCandidate("count-userB@test.com", "password123");

        createNotification("count-userA@test.com", false);
        createNotification("count-userB@test.com", false);
        createNotification("count-userB@test.com", false);

        // User A chỉ thấy 1, không thấy 2 của user B
        mockMvc.perform(get("/api/notifications/unread-count").cookie(tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/notifications
    // ─────────────────────────────────────────────────────────────

    @Test
    void getNotifications_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getNotifications_emptyList_returnsEmptyPage() throws Exception {
        Cookie token = registerAndLoginCandidate("list-empty@test.com", "password123");

        mockMvc.perform(get("/api/notifications").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getNotifications_returnsPaginatedList() throws Exception {
        Cookie token = registerAndLoginCandidate("list-paged@test.com", "password123");

        createNotification("list-paged@test.com", false);
        createNotification("list-paged@test.com", true);

        mockMvc.perform(get("/api/notifications").cookie(token)
                .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].message").exists())
                .andExpect(jsonPath("$.content[0].isRead").exists());
    }

    @Test
    void getNotifications_onlyReturnsCurrentUserNotifications() throws Exception {
        Cookie tokenA = registerAndLoginCandidate("list-userA@test.com", "password123");
        registerAndLoginCandidate("list-userB@test.com", "password123");

        createNotification("list-userA@test.com", false);
        createNotification("list-userB@test.com", false);
        createNotification("list-userB@test.com", false);

        // User A chỉ thấy 1 notification của mình
        mockMvc.perform(get("/api/notifications").cookie(tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/notifications/{id}/read
    // ─────────────────────────────────────────────────────────────

    @Test
    void markAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/notifications/" + UUID.randomUUID() + "/read"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAsRead_ownNotification_returns204AndSetsReadAt() throws Exception {
        Cookie token = registerAndLoginCandidate("read-own@test.com", "password123");
        Notification n = createNotification("read-own@test.com", false);

        mockMvc.perform(patch("/api/notifications/" + n.getId() + "/read").cookie(token))
                .andExpect(status().isNoContent());

        Notification updated = notificationRepository.findById(n.getId()).orElseThrow();
        assertThat(updated.getReadAt()).isNotNull();
    }

    @Test
    void markAsRead_alreadyRead_returns204Idempotent() throws Exception {
        Cookie token = registerAndLoginCandidate("read-already@test.com", "password123");
        Notification n = createNotification("read-already@test.com", true);
        OffsetDateTime originalReadAt = n.getReadAt();

        mockMvc.perform(patch("/api/notifications/" + n.getId() + "/read").cookie(token))
                .andExpect(status().isNoContent());

        // readAt không bị ghi đè
        Notification updated = notificationRepository.findById(n.getId()).orElseThrow();
        assertThat(updated.getReadAt()).isEqualToIgnoringNanos(originalReadAt);
    }

    @Test
    void markAsRead_otherUserNotification_returns403() throws Exception {
        // Tạo notification cho user B
        registerAndLoginCandidate("read-owner@test.com", "password123");
        Notification n = createNotification("read-owner@test.com", false);

        // User A cố đánh dấu đọc notification của user B → 403 Forbidden
        Cookie tokenA = registerAndLoginCandidate("read-attacker@test.com", "password123");
        mockMvc.perform(patch("/api/notifications/" + n.getId() + "/read").cookie(tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAsRead_nonExistentId_returns404() throws Exception {
        Cookie token = registerAndLoginCandidate("read-notexist@test.com", "password123");

        mockMvc.perform(patch("/api/notifications/" + UUID.randomUUID() + "/read").cookie(token))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/notifications/read-all
    // ─────────────────────────────────────────────────────────────

    @Test
    void markAllAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAllAsRead_returns204AndMarkAllUnread() throws Exception {
        Cookie token = registerAndLoginCandidate("readall@test.com", "password123");

        createNotification("readall@test.com", false);
        createNotification("readall@test.com", false);
        createNotification("readall@test.com", true); // đã đọc sẵn

        mockMvc.perform(patch("/api/notifications/read-all").cookie(token))
                .andExpect(status().isNoContent());

        long remaining = notificationRepository.countByUserIdAndReadAtIsNull(
                userRepository.findByEmail("readall@test.com").orElseThrow().getId());
        assertThat(remaining).isZero();
    }

    @Test
    void markAllAsRead_onlyAffectsCurrentUser() throws Exception {
        Cookie tokenA = registerAndLoginCandidate("readall-userA@test.com", "password123");
        registerAndLoginCandidate("readall-userB@test.com", "password123");

        createNotification("readall-userA@test.com", false);
        createNotification("readall-userB@test.com", false);

        // User A đọc hết → notification của user B không bị ảnh hưởng
        mockMvc.perform(patch("/api/notifications/read-all").cookie(tokenA))
                .andExpect(status().isNoContent());

        long userBUnread = notificationRepository.countByUserIdAndReadAtIsNull(
                userRepository.findByEmail("readall-userB@test.com").orElseThrow().getId());
        assertThat(userBUnread).isEqualTo(1);
    }
}
