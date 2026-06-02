package com.yoedu.job_board_platform.models;

/**
 * Trạng thái của thông báo trên nền tảng
 * - <strong>Application Status Changed:</strong> Thông báo về thay đổi trạng thái của đơn ứng tuyển <i>(dành cho ứng viên)</i>
 * - <strong>Application Received:</strong> Thông báo về việc nhận được đơn ứng tuyển mới <i>(dành cho nhà tuyển dụng)</i>
 * - <strong>Job Status Changed:</strong> Thông báo về thay đổi trạng thái của tin tuyển dụng <i>(dành cho nhà tuyển dụng)</i>
 * - <strong>Company Status Changed:</strong> Thông báo về thay đổi trạng thái của công ty <i>(dành cho nhà tuyển dụng)</i>
 * - <strong>Company Pending Review:</strong> Thông báo về công ty mới đăng ký đang chờ duyệt <i>(dành cho quản trị viên)</i>
 * - <strong>Job Pending Review:</strong> Thông báo về tin tuyển dụng mới đang chờ duyệt <i>(dành cho quản trị viên)</i>
 */
public enum NotificationStatus {
    // Dành cho ứng viên
    APPLICATION_STATUS_CHANGED,

    // Dành cho nhà tuyển dụng
    APPLICATION_RECEIVED,
    JOB_STATUS_CHANGED,
    COMPANY_STATUS_CHANGED,

    // Dành cho quản trị viên
    COMPANY_PENDING_REVIEW,
    JOB_PENDING_REVIEW,
}
