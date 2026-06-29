package com.yoedu.job_board_platform.repositories;

import com.yoedu.job_board_platform.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
	Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

	long countByUserIdAndReadAtIsNull(UUID userId);

	@Modifying
	@Transactional
	@Query("UPDATE Notification n SET n.readAt = :now WHERE n.user.id = :userId AND n.readAt IS NULL")
	int markAllAsRead(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

	Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}

