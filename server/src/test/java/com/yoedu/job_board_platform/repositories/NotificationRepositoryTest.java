package com.yoedu.job_board_platform.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.yoedu.job_board_platform.TestcontainersConfiguration;
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.utils.DatabaseCleaner;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class NotificationRepositoryTest {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager entityManager;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.cleanAllTables(jdbcTemplate, entityManager);

        userA = userRepository.save(User.builder()
                .email("userA-repo@test.com")
                .password("hashed")
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build());

        userB = userRepository.save(User.builder()
                .email("userB-repo@test.com")
                .password("hashed")
                .role(UserRole.CANDIDATE)
                .isActive(true)
                .build());
    }

    private Notification saveNotification(User user, boolean isRead) {
        Notification n = Notification.builder()
                .user(user)
                .type(NotificationStatus.APPLICATION_STATUS_CHANGED)
                .entityId(UUID.randomUUID())
                .message("Test notification")
                .build();
        if (isRead) n.setReadAt(OffsetDateTime.now());
        return notificationRepository.save(n);
    }

    // ─────────────────────────────────────────────────────────────
    // countByUserIdAndReadAtIsNull
    // ─────────────────────────────────────────────────────────────

    @Test
    void countByUserIdAndReadAtIsNull_returnsOnlyUnreadOfUser() {
        saveNotification(userA, false);
        saveNotification(userA, false);
        saveNotification(userA, true);  // đã đọc, không tính
        saveNotification(userB, false); // user khác, không tính

        long count = notificationRepository.countByUserIdAndReadAtIsNull(userA.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByUserIdAndReadAtIsNull_returnsZeroWhenNone() {
        long count = notificationRepository.countByUserIdAndReadAtIsNull(userA.getId());

        assertThat(count).isZero();
    }

    // ─────────────────────────────────────────────────────────────
    // findByUserIdOrderByCreatedAtDesc
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsOnlyCurrentUserNotifications() {
        saveNotification(userA, false);
        saveNotification(userA, true);
        saveNotification(userB, false); // không thuộc userA

        Page<Notification> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userA.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(n -> n.getUser().getId().equals(userA.getId()));
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_paginationWorks() {
        for (int i = 0; i < 5; i++) saveNotification(userA, false);

        Page<Notification> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userA.getId(), PageRequest.of(0, 3));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_emptyWhenNoNotifications() {
        Page<Notification> page = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userA.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    // ─────────────────────────────────────────────────────────────
    // findByIdAndUserId
    // ─────────────────────────────────────────────────────────────

    @Test
    void findByIdAndUserId_returnsNotificationWhenOwnerMatches() {
        Notification n = saveNotification(userA, false);

        Optional<Notification> result = notificationRepository
                .findByIdAndUserId(n.getId(), userA.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(n.getId());
    }

    @Test
    void findByIdAndUserId_emptyWhenWrongOwner() {
        Notification n = saveNotification(userA, false);

        Optional<Notification> result = notificationRepository
                .findByIdAndUserId(n.getId(), userB.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_emptyWhenIdNotExist() {
        Optional<Notification> result = notificationRepository
                .findByIdAndUserId(UUID.randomUUID(), userA.getId());

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // markAllAsRead
    // ─────────────────────────────────────────────────────────────

    @Test
    void markAllAsRead_setsReadAtForAllUnread() {
        saveNotification(userA, false);
        saveNotification(userA, false);
        saveNotification(userA, true);

        int updated = notificationRepository.markAllAsRead(userA.getId(), OffsetDateTime.now());

        assertThat(updated).isEqualTo(2);
        long remaining = notificationRepository.countByUserIdAndReadAtIsNull(userA.getId());
        assertThat(remaining).isZero();
    }

    @Test
    void markAllAsRead_doesNotAffectOtherUsers() {
        saveNotification(userA, false);
        saveNotification(userB, false);

        notificationRepository.markAllAsRead(userA.getId(), OffsetDateTime.now());

        long userBUnread = notificationRepository.countByUserIdAndReadAtIsNull(userB.getId());
        assertThat(userBUnread).isEqualTo(1);
    }

    @Test
    void markAllAsRead_returnsZeroWhenNothingToUpdate() {
        saveNotification(userA, true); // đã đọc rồi

        int updated = notificationRepository.markAllAsRead(userA.getId(), OffsetDateTime.now());

        assertThat(updated).isZero();
    }
}
