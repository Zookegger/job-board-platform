package com.yoedu.job_board_platform.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yoedu.job_board_platform.models.CompanyApprovalLog;

public interface CompanyApprovalLogRepository extends JpaRepository<CompanyApprovalLog, UUID> {

    @Query("SELECT l FROM CompanyApprovalLog l WHERE l.company.id = :companyId ORDER BY l.createdAt DESC")
    List<CompanyApprovalLog> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") UUID companyId);
}
