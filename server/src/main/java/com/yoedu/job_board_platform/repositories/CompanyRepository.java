package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Company findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
    boolean existsByCompanyName(String companyName);
    
    /**
     * Tìm các công ty có trạng thái PENDING cần duyệt.
     * @param status trạng thái CompanyStatus.PENDING
     * @param pageable thông tin phân trang
     * @return danh sách công ty đang chờ duyệt
     */
    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);
}
