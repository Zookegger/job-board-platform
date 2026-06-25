package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.ApplicationStatusLog;

@Repository
public interface ApplicationStatusLogRepository extends JpaRepository<ApplicationStatusLog, UUID> {

    List<ApplicationStatusLog> findByApplicationIdOrderByChangedAtAsc(UUID applicationId);
}
