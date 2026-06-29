package com.yoedu.job_board_platform.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

	private final ObjectMapper objectMapper = new ObjectMapper();

	private Cookie authCookie;
	private User currentUser;
	private UUID entityId;

	@BeforeEach
	void setUp() throws Exception {
		DatabaseCleaner.cleanAllTables(jdbcTemplate, entityManager);

		entityId = UUID.randomUUID();

		authCookie = registerAndLogin("notif-test@test.com", "password123");
		currentUser = userRepository.findByEmail("notif-test@test.com").orElseThrow();
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

		var loginPayload = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
		MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginPayload))
				.andExpect(status().isOk())
				.andReturn();
		return loginRes.getResponse().getCookie("accessToken");
	}

	private Notification createNotification(NotificationStatus type, UUID entityId) {
		Notification notification = Notification.builder()
				.user(currentUser)
				.type(type)
				.entityId(entityId)
				.message("Test notification: " + type.name())
				.readAt(null)
				.build();
		return notificationRepository.save(notification);
	}

	// -----------------------------------------------------------------------
	// GET /api/notifications — paginated list
	// -----------------------------------------------------------------------

	@Test
	void getNotifications_withAuth_returnsPaginatedList() throws Exception {
		createNotification(NotificationStatus.APPLICATION_STATUS_CHANGED, entityId);
		createNotification(NotificationStatus.COMPANY_STATUS_CHANGED, UUID.randomUUID());

		mockMvc.perform(get("/api/notifications").cookie(authCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	void getNotifications_withAuth_returnsEmptyPage() throws Exception {
		mockMvc.perform(get("/api/notifications").cookie(authCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0))
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void getNotifications_withoutAuth_returns401() throws Exception {
		mockMvc.perform(get("/api/notifications"))
				.andExpect(status().isUnauthorized());
	}

	// -----------------------------------------------------------------------
	// GET /api/notifications/unread-count
	// -----------------------------------------------------------------------

	@Test
	void getUnreadCount_withAuth_returnsCount() throws Exception {
		createNotification(NotificationStatus.APPLICATION_STATUS_CHANGED, entityId);
		createNotification(NotificationStatus.COMPANY_STATUS_CHANGED, UUID.randomUUID());

		mockMvc.perform(get("/api/notifications/unread-count").cookie(authCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(2));
	}

	@Test
	void getUnreadCount_withAuth_zeroWhenAllRead() throws Exception {
		Notification n = createNotification(NotificationStatus.APPLICATION_STATUS_CHANGED, entityId);
		n.setReadAt(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		notificationRepository.save(n);

		mockMvc.perform(get("/api/notifications/unread-count").cookie(authCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(0));
	}

	@Test
	void getUnreadCount_withoutAuth_returns401() throws Exception {
		mockMvc.perform(get("/api/notifications/unread-count"))
				.andExpect(status().isUnauthorized());
	}

	// -----------------------------------------------------------------------
	// PUT /api/notifications/{id}/read
	// -----------------------------------------------------------------------

	@Test
	void markAsRead_withAuth_returns204() throws Exception {
		Notification n = createNotification(NotificationStatus.APPLICATION_STATUS_CHANGED, entityId);

		mockMvc.perform(put("/api/notifications/{id}/read", n.getId()).cookie(authCookie))
				.andExpect(status().isNoContent());
	}

	@Test
	void markAsRead_withoutAuth_returns401() throws Exception {
		Notification n = createNotification(NotificationStatus.APPLICATION_STATUS_CHANGED, entityId);

		mockMvc.perform(put("/api/notifications/{id}/read", n.getId()))
				.andExpect(status().isUnauthorized());
	}

	// -----------------------------------------------------------------------
	// PUT /api/notifications/read-all
	// -----------------------------------------------------------------------

	@Test
	void markAllAsRead_withAuth_returns204() throws Exception {
		createNotification(NotificationStatus.APPLICATION_STATUS_CHANGED, entityId);
		createNotification(NotificationStatus.COMPANY_STATUS_CHANGED, UUID.randomUUID());

		mockMvc.perform(put("/api/notifications/read-all").cookie(authCookie))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/notifications/unread-count").cookie(authCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(0));
	}

	@Test
	void markAllAsRead_withoutAuth_returns401() throws Exception {
		mockMvc.perform(put("/api/notifications/read-all"))
				.andExpect(status().isUnauthorized());
	}
}
