package com.yoedu.job_board_platform.repositories;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    Page<Job> findByCompanyIdAndStatus(UUID CompanyId, JobStatus jobStatus, Pageable pageable);

    Page<Job> findByCompanyId(UUID CompanyId, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE Job j SET j.status = :newStatus WHERE j.status = :currentStatus AND j.expirationDate IS NOT NULL AND j.expirationDate < :now")
    int expireActiveJobs(@Param("currentStatus") JobStatus currentStatus,
                         @Param("newStatus") JobStatus newStatus,
                         @Param("now") OffsetDateTime now);
}
