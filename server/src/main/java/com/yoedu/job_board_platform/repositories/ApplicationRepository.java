package com.yoedu.job_board_platform.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Page<Application> findByCandidateId(UUID candidateId, Pageable pageable);

    Page<Application> findByCandidateIdAndStatus(
            UUID candidateId, ApplicationStatus status, Pageable pageable);

    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    boolean existsByCandidateIdAndJobIdAndStatusNot(UUID candidateId, UUID jobId, ApplicationStatus status);

    Optional<Application> findByCandidateIdAndJobId(UUID candidateId, UUID jobId);

    // --- Employer queries ---
    Page<Application> findByJobCompanyId(UUID companyId, Pageable pageable);

    Page<Application> findByJobCompanyIdAndStatus(UUID companyId, ApplicationStatus status, Pageable pageable);

    Page<Application> findByJobCompanyIdAndJobId(UUID companyId, UUID jobId, Pageable pageable);

    Page<Application> findByJobCompanyIdAndJobIdAndStatus(
            UUID companyId, UUID jobId, ApplicationStatus status, Pageable pageable);
}
