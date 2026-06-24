package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.yoedu.job_board_platform.dtos.application.ApplicationListResponse;
import com.yoedu.job_board_platform.models.ApplicationStatus;

/**
 * Service quản lý đơn ứng tuyển của ứng viên.
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
            UUID candidateId, ApplicationStatus status, int page, int size);
}
