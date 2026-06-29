package com.yoedu.job_board_platform.repositories;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    long countByUserIdAndReadAtIsNull(UUID userId);

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Notification> findByIdAndUser_Id(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.user.id = :userId AND n.readAt IS NULL")
    int markAllAsReadByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}

