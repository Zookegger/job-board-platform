package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);
}
