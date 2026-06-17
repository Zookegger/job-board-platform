package com.yoedu.job_board_platform.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.CompanyEmployerDetail;

@Repository
public interface CompanyEmployerDetailRepository extends JpaRepository<CompanyEmployerDetail, UUID> {
    CompanyEmployerDetail findByCompanyId(UUID companyId);
    
    /**
     * Tìm tất cả các HR của một công ty.
     * @param companyId ID của công ty
     * @return danh sách các employer detail
     */
    List<CompanyEmployerDetail> findAllByCompanyId(UUID companyId);
}
