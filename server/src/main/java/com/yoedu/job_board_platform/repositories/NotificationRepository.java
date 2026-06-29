package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    long countByUserIdAndReadAtIsNull(UUID userId);
}

