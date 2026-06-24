package com.yoedu.job_board_platform.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.ReportStatus;

/**
 * Repository cho entity Report.
 * Hỗ trợ truy vấn báo cáo theo trạng thái và truy vấn động với Specification.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

    /**
     * Tìm báo cáo theo trạng thái, phân trang.
     *
     * @param status   trạng thái báo cáo (PENDING, REVIEWED, DISMISSED, RESOLVED)
     * @param pageable thông tin phân trang và sắp xếp
     * @return trang báo cáo
     */
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Đếm số báo cáo theo trạng thái.
     *
     * @param status trạng thái báo cáo
     * @return số lượng báo cáo
     */
    long countByStatus(ReportStatus status);
}
