package com.yoedu.job_board_platform.models;

/**
 * Lý do công ty cần được duyệt/kiểm tra lại.
 * - {@code NEW_COMPANY}: Công ty mới đăng ký, chưa từng được duyệt.
 * - {@code INFO_UPDATED}: Công ty đã được duyệt nhưng vừa cập nhật thông tin
 *   (companyName hoặc taxCode) cần được duyệt lại.
 */
public enum CompanyReviewReason {
    NEW_COMPANY,
    INFO_UPDATED
}
