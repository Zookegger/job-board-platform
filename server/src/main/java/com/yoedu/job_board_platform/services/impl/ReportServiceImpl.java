package com.yoedu.job_board_platform.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.report.CreateReportRequest;
import com.yoedu.job_board_platform.dtos.report.ReportResponse;
import com.yoedu.job_board_platform.mappers.ReportMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.ReportRepository;
import com.yoedu.job_board_platform.services.ReportService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai nghiệp vụ báo cáo vi phạm.
 * Xử lý validation, kiểm tra tồn tại của target, lưu báo cáo vào database.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final ReportMapper reportMapper;
    private final SecurityUtil securityUtil;

    @Override
    public ReportResponse createReport(CreateReportRequest request) {
        if ((request.jobId() == null && request.companyId() == null) ||
                (request.jobId() != null && request.companyId() != null)) {
            throw new BadRequestException("Phải báo cáo một tin tuyển dụng hoặc một công ty (không thể cả hai)");
        }

        User currentUser = securityUtil.getCurrentUser();

        Report report;
        if (request.jobId() != null) {
            Job job = jobRepository.findById(request.jobId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy bài tuyển dụng với ID: " + request.jobId()));
            report = reportMapper.toEntity(request, currentUser, job, null);
        } else {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy công ty với ID: " + request.companyId()));
            report = reportMapper.toEntity(request, currentUser, null, company);
        }

        report = reportRepository.save(report);
        log.info("Tạo báo cáo vi phạm: reportId={}, reason={}, reportedBy={}", report.getId(), report.getReason(), currentUser.getId());
        return reportMapper.toResponse(report);
    }
}
