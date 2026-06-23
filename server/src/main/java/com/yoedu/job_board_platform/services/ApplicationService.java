package com.yoedu.job_board_platform.services;

import java.util.UUID;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;

/**
 * Service xử lý nộp đơn ứng tuyển của ứng viên.
 */
public interface ApplicationService {

    /**
     * Nộp đơn ứng tuyển vào một tin tuyển dụng.
     * Chỉ cho phép nộp khi job đang ACTIVE.
     * Mỗi ứng viên chỉ được nộp một đơn cho mỗi job.
     *
     * @param request thông tin đơn ứng tuyển (jobId, coverLetter)
     * @return ApplicationResponse thông tin đơn sau khi nộp
     * @throws NotFoundException   nếu không tìm thấy job hoặc profile ứng viên
     * @throws BadRequestException nếu job không ACTIVE hoặc đã nộp đơn rồi
     */
    ApplicationResponse submitApplication(ApplicationRequest request);

    /**
     * Kiểm tra ứng viên hiện tại đã nộp đơn vào một job chưa.
     *
     * @param jobId UUID của tin tuyển dụng
     * @return true nếu đã nộp đơn, false nếu chưa
     */
    boolean checkApplied(UUID jobId);
}
