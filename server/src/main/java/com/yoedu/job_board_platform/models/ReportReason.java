package com.yoedu.job_board_platform.models;

/**
 * Lý do báo cáo bài viết hoặc công ty trên nền tảng
 * - <strong>Spam:</strong> Nội dung là spam, quảng cáo hoặc không liên quan
 * - <strong>Scam:</strong> Nội dung có dấu hiệu lừa đảo, gian lận
 * - <strong>Inappropriate Content:</strong> Nội dung không phù hợp, vi phạm quy tắc cộng đồng
 * - <strong>Other:</strong> Lý do khác, người dùng có thể cung cấp thêm thông tin chi tiết
 */
public enum ReportReason {
    SPAM,
    SCAM,
    INAPPROPRIATE,
    OTHER
}
