package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.models.ApplicationStatus;

/**
 * Service xử lý nộp đơn ứng tuyển của ứng viên.
 */
public interface ApplicationService {

    /**
     * Lấy danh sách đơn ứng tuyển của ứng viên hiện tại (có phân trang).
     * Có thể lọc theo trạng thái, sắp xếp theo ngày nộp mới nhất.
     *
     * @param candidateId UUID của ứng viên
     * @param status      lọc theo trạng thái (có thể null để lấy tất cả)
     * @param page        số trang (bắt đầu từ 0)
     * @param size        số phần tử mỗi trang
     * @return trang dữ liệu ApplicationListResponse
     */
    Page<ApplicationListResponse> getCandidateApplications(
            UUID candidateId, ApplicationStatus status, Pageable pageable);

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
     * @return true nếu đã nộp đơn (chưa rút), false nếu chưa
     */
    boolean checkApplied(UUID jobId);

    /**
     * Rút đơn ứng tuyển. Chỉ được rút khi đơn ở trạng thái PENDING.
     * Sau khi rút, ứng viên có thể nộp lại.
     *
     * @param id UUID của đơn ứng tuyển
     */
    void withdrawApplication(UUID id);

    /**
     * Lấy UUID của đơn ứng tuyển theo jobId (nếu có).
     *
     * @param jobId UUID của tin tuyển dụng
     * @return UUID của đơn, hoặc null nếu chưa nộp
     */
    UUID getApplicationIdByJob(UUID jobId);
}
