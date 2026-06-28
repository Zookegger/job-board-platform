package com.yoedu.job_board_platform.repositories;

import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompanyIdAndStatus(UUID companyId, JobStatus jobStatus, Pageable pageable);

    Page<Job> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    long countByStatus(JobStatus status);

    long countByCompanyIdAndStatus(UUID companyId, JobStatus status);

    @Query("SELECT DISTINCT j.category FROM Job j WHERE j.company.id = :companyId AND j.status = :status")
    List<JobCategory> findDistinctCategoriesByCompanyIdAndStatus(@Param("companyId") UUID companyId,
            @Param("status") JobStatus status);

    @Query("SELECT j.company.id, j.category FROM Job j WHERE j.company.id IN :companyIds AND j.status = :status GROUP BY j.company.id, j.category")
    List<Object[]> findDistinctCategoriesByCompanyIdsAndStatus(@Param("companyIds") List<UUID> companyIds,
            @Param("status") JobStatus status);

    boolean existsBySlug(String slug);

    Optional<Job> findBySlugAndStatus(String slug, JobStatus status);

    @Modifying
    @Query("UPDATE Job j SET j.status = :newStatus WHERE j.status = :currentStatus AND j.expirationDate IS NOT NULL AND j.expirationDate < :now")
    int expireActiveJobs(@Param("currentStatus") JobStatus currentStatus,
                         @Param("newStatus") JobStatus newStatus,
                         @Param("now") OffsetDateTime now);

    @Query("""
        SELECT j FROM Job j
        WHERE j.category.id = :categoryId
        AND j.id != :currentJobId
        AND j.status = 'ACTIVE'
        AND (j.expirationDate IS NULL OR j.expirationDate >= CURRENT_TIMESTAMP)
        AND (
                    COALESCE(:skillIds, NULL) IS NULL
                    OR NOT EXISTS (SELECT 1 FROM JobSkill js2 WHERE js2.jobId = j.id)
                    OR EXISTS (SELECT 1 FROM JobSkill js WHERE js.jobId = j.id AND js.skillId IN :skillIds)
        )
        ORDER BY j.createdAt DESC
    """)
    List<Job> findRelatedJobsWithSkills(@Param("categoryId") Integer categoryId,
                                        @Param("skillIds") Set<Integer> skillIds,
                                        @Param("currentJobId") UUID currentJobId,
                                        Pageable pageable);

    @Query("""
        SELECT j FROM Job j
        WHERE j.category.id = :categoryId
        AND j.id != :currentJobId
        AND j.status = 'ACTIVE'
        AND (j.expirationDate IS NULL OR j.expirationDate >= CURRENT_TIMESTAMP)
        ORDER BY j.createdAt DESC
    """)
    List<Job> findRelatedJobsNoSkills(@Param("categoryId") Integer categoryId,
                                      @Param("currentJobId") UUID currentJobId,
                                      Pageable pageable);
}