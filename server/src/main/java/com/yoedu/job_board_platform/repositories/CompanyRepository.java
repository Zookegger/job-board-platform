package com.yoedu.job_board_platform.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.JobStatus;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    Optional<Company> findByEmail(String email);

    Optional<Company> findBySlug(String slug);

    boolean existsByEmail(String email);

    boolean existsBySlug(String slug);

    boolean existsByCompanyName(String companyName);

    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);

    long countByStatus(CompanyStatus status);

    Optional<Company> findByIdAndIsApprovedTrue(UUID id);

    Optional<Company> findByIdAndStatusAndIsApprovedTrue(UUID id, CompanyStatus status);

    Optional<Company> findBySlugAndStatusAndIsApprovedTrue(String slug, CompanyStatus status);

    @Query("""
                SELECT j.company.id, COUNT(j) FROM Job j
                WHERE j.company.id IN :companyIds AND j.status = :status
                GROUP BY j.company.id
            """)
    List<Object[]> countByCompanyIdsAndStatus(@Param("companyIds") List<UUID> companyIds,
            @Param("status") JobStatus status);
}