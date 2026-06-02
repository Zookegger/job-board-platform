package com.yoedu.job_board_platform.models;

/**
 * Trạng thái ứng tuyen của ứng viên đối với một vị trí công việc
 * - <strong>Pending:</strong> Ứng viên đã nộp đơn nhưng chưa được nhà tuyển dụng xem xét
 * - <strong>Reviewing:</strong> Nhà tuyển dụng đang xem xét đơn ứng tuyển
 * - <strong>Interview:</strong> Ứng viên đã được chọn phỏng vấn
 * - <strong>Hired:</strong> Ứng viên đã được tuyển dụng
 * - <strong>Rejected:</strong> Ứng viên đã bị từ chối
 */
public enum ApplicationStatus {
    PENDING,
    REVIEWING,
    INTERVIEW,
    HIRED,
    REJECTED
}
