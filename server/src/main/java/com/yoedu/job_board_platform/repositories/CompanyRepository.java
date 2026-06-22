package com.yoedu.job_board_platform.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID>, JpaSpecificationExecutor<Company> {

    Company findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsBySlug(String slug);

    boolean existsByCompanyName(String companyName);

    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);

    Optional<Company> findByIdAndIsApprovedTrue(UUID id);

    Optional<Company> findByIdAndStatusAndIsApprovedTrue(UUID id, CompanyStatus status);

    Optional<Company> findBySlugAndStatusAndIsApprovedTrue(String slug, CompanyStatus status);
}