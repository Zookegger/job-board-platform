package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Page<Application> findByCandidate_Id(UUID candidateId, Pageable pageable);

    Page<Application> findByCandidate_IdAndStatus(
            UUID candidateId, ApplicationStatus status, Pageable pageable);
}
