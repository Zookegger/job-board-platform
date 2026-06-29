package com.yoedu.job_board_platform.repositories;

import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyEmployerDetailRepository extends JpaRepository<CompanyEmployerDetail, UUID> {
    @EntityGraph(attributePaths = {"company", "profile", "profile.user"})
    List<CompanyEmployerDetail> findByCompanyIdIn(Collection<UUID> companyIds);

    List<CompanyEmployerDetail> findAllByCompanyId(UUID companyId);
}
