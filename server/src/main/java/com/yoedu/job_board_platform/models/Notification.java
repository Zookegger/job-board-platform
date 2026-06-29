package com.yoedu.job_board_platform.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Thông báo gửi đến người dùng. Mỗi thông báo thuộc một loại (NotificationStatus)
 * và liên kết với một thực thể cụ thể (entityId).
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    @Id
    @Column(columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus type;

    @Column(name = "entity_id", columnDefinition = "uuid", nullable = false)
    private UUID entityId;

    @Column(columnDefinition = "text", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;
}
