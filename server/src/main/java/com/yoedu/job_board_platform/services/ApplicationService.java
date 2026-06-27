package com.yoedu.job_board_platform.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.application.ApplicationCheckResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationRequest;
import com.yoedu.job_board_platform.dtos.application.ApplicationResponse;
import com.yoedu.job_board_platform.dtos.application.ApplicationTimelineResponse;
import com.yoedu.job_board_platform.dtos.application.EmployerApplicationListResponse;
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

    /**
     * Kiểm tra trạng thái ứng tuyển của ứng viên hiện tại cho một job.
     * Trả về DTO gồm applied (boolean) và applicationId (UUID hoặc null).
     *
     * @param jobId UUID của tin tuyển dụng
     * @return ApplicationCheckResponse với applied và applicationId
     */
    ApplicationCheckResponse checkApplicationByJob(UUID jobId);

    /**
     * Lấy chi tiết một đơn ứng tuyển.
     * Chỉ cho phép xem đơn của chính mình.
     *
     * @param id UUID của đơn ứng tuyển
     * @return ApplicationResponse thông tin chi tiết đơn
     * @throws NotFoundException nếu không tìm thấy đơn
     * @throws ForbiddenException nếu không phải đơn của mình
     */
    ApplicationResponse getApplicationDetail(UUID id);

    /**
     * Lấy lịch sử thay đổi trạng thái của một đơn ứng tuyển.
     *
     * @param applicationId UUID của đơn ứng tuyển
     * @return danh sách các bản ghi thay đổi trạng thái theo thứ tự thời gian
     */
    List<ApplicationTimelineResponse> getTimeline(UUID applicationId);

    /**
     * Lấy danh sách đơn ứng tuyển theo công ty (dành cho Employer).
     * Có thể lọc thêm theo jobId và status.
     *
     * @param companyId UUID của công ty
     * @param jobId     lọc theo tin tuyển dụng (có thể null)
     * @param status    lọc theo trạng thái (có thể null)
     * @param pageable  thông tin phân trang
     * @return trang dữ liệu EmployerApplicationListResponse
     */
    Page<EmployerApplicationListResponse> getEmployerApplications(
            UUID companyId, UUID jobId, ApplicationStatus status, Pageable pageable);

    /**
     * Cập nhật trạng thái đơn ứng tuyển (dành cho Employer).
     * Kiểm tra quyền sở hữu và tính hợp lệ của trạng thái đích.
     *
     * @param applicationId UUID của đơn ứng tuyển
     * @param newStatus     trạng thái mới (REVIEWING, INTERVIEW, HIRED, REJECTED)
     * @param reason        lý do (tuỳ chọn)
     * @throws ForbiddenException  nếu employer không sở hữu bài đăng này
     * @throws BadRequestException nếu trạng thái không hợp lệ
     */
    void updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus, String reason);
}
