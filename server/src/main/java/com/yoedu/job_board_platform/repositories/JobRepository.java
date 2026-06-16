package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    Page<Job> findByCompanyIdAndStatus(UUID CompanyId, JobStatus jobStatus, Pageable pageable);

    Page<Job> findByCompanyId(UUID CompanyId, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    boolean existsBySlug(String slug);
}
