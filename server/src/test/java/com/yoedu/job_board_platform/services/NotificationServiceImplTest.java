package com.yoedu.job_board_platform.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.impl.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private final UUID userId = UUID.randomUUID();
    private final UUID entityId = UUID.randomUUID();
    private final UUID notificationId = UUID.randomUUID();

    @Test
    void createNotification_savesWithCorrectValues() {
        User user = User.builder().id(userId).build();
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        notificationService.createNotification(userId, NotificationStatus.APPLICATION_STATUS_CHANGED, entityId,
                "Test message");

        verify(notificationRepository).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();
        assertThat(saved.getUser().getId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(NotificationStatus.APPLICATION_STATUS_CHANGED);
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        assertThat(saved.getMessage()).isEqualTo("Test message");
        assertThat(saved.getReadAt()).isNull();
    }

    @Test
    void getNotifications_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> expectedPage = new PageImpl<>(java.util.List.of());
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(expectedPage);

        Page<Notification> result = notificationService.getNotifications(userId, pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void getUnreadCount_returnsCountFromRepository() {
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(5L);
        verify(notificationRepository).countByUserIdAndReadAtIsNull(userId);
    }

    @Test
    void markAsRead_setsReadAtOnExistingNotification() {
        Notification notification = Notification.builder()
                .id(notificationId)
                .user(User.builder().id(userId).build())
                .readAt(null)
                .build();
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(userId, notificationId);

        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();
        assertThat(savedNotification.getReadAt()).isNotNull();
    }

    @Test
    void markAsRead_doesNothingWhenNotificationNotFound() {
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.empty());

        notificationService.markAsRead(userId, notificationId);

        verify(notificationRepository).findByIdAndUserId(notificationId, userId);
    }

    @Test
    void markAllAsRead_callsRepositoryMethod() {
        when(notificationRepository.markAllAsRead(eq(userId), any(OffsetDateTime.class))).thenReturn(3);

        notificationService.markAllAsRead(userId);

        verify(notificationRepository).markAllAsRead(eq(userId), any(OffsetDateTime.class));
    }
}
