package com.yoedu.job_board_platform.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompanyIdAndStatus(UUID companyId, JobStatus jobStatus, Pageable pageable);

    Page<Job> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    long countByCompanyIdAndStatus(UUID companyId, JobStatus status);

    @Query("SELECT DISTINCT j.category FROM Job j WHERE j.company.id = :companyId AND j.status = :status")
    List<JobCategory> findDistinctCategoriesByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") JobStatus status);

    @Query("SELECT j.company.id, j.category FROM Job j WHERE j.company.id IN :companyIds AND j.status = :status GROUP BY j.company.id, j.category")
    List<Object[]> findDistinctCategoriesByCompanyIdsAndStatus(@Param("companyIds") List<UUID> companyIds, @Param("status") JobStatus status);

    boolean existsBySlug(String slug);

}