package com.yoedu.job_board_platform.models;

/**
 * Trạng thái của báo cáo trên nền tảng
 * - <strong>Pending:</strong> Báo cáo mới được tạo và đang chờ
 * - <strong>Reviewed:</strong> Báo cáo đã được xem xét bởi quản trị viên nhưng chưa có hành động cụ thể nào được thực hiện.
 * - <strong>Resolved:</strong> Báo cáo đã được giải quyết, có thể là đã xóa nội dung vi phạm, cảnh cáo người dùng, hoặc các biện pháp khác tùy thuộc vào mức độ nghiêm trọng của vi phạm.
 */
public enum ReportStatus {
    PENDING, REVIEWED, RESOLVED
}
