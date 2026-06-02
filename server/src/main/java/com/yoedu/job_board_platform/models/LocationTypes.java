package com.yoedu.job_board_platform.models;

/**
 * Danh sách các loại hình làm việc, giúp phân loại và hiển thị thông tin về vị trí công việc.
 * - <strong>Remote:</strong> Làm việc từ xa, không yêu cầu có mặt tại văn phòng. Ứng viên có thể làm việc từ bất kỳ đâu.
 * - <strong>Hybrid:</strong> Làm việc kết hợp giữa làm việc tại văn phòng và làm việc từ xa. Ứng viên có thể linh hoạt lựa chọn giữa hai hình thức này.
 * - <strong>On-site:</strong> Làm việc tại văn phòng, yêu cầu ứng viên phải có mặt tại địa điểm làm việc. Thường áp dụng cho các vị trí cần sự tương tác trực tiếp hoặc sử dụng thiết bị tại chỗ.
 */
public enum LocationTypes {
    REMOTE,
    HYBRID,
    ONSITE
}
