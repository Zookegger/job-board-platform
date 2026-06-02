package com.yoedu.job_board_platform.models;

/**
 * Danh sách các cấp độ kinh nghiệm, giúp phân loại và hiển thị thông tin về mức độ kinh nghiệm yêu cầu cho vị trí công việc.
 * - <strong>Not Required:</strong> Không yêu cầu kinh nghiệm, phù hợp cho các vị trí entry-level hoặc thực tập.
 * - <strong>Intern:</strong> Thực tập, dành cho sinh viên hoặc những người mới bắt đầu sự nghiệp, thường có thời gian làm việc ngắn hạn.
 * - <strong>Junior:</strong> Cấp độ thấp, dành cho những người có ít kinh nghiệm làm việc (thường dưới 2 năm), cần sự hướng dẫn và hỗ trợ từ cấp trên.
 * - <strong>Mid:</strong> Cấp độ trung bình, dành cho những người có kinh nghiệm làm việc từ 2 đến 5 năm, có khả năng làm việc độc lập và đóng góp vào dự án.
 * - <strong>Senior:</strong> Cấp độ cao, dành cho những người có kinh nghiệm làm việc trên 5 năm, có khả năng lãnh đạo dự án và hướng dẫn junior.
 * - <strong>Lead:</strong> Cấp độ lãnh đạo, dành cho những người có kinh nghiệm làm việc nhiều năm và có khả năng quản lý nhóm hoặc dự án lớn.
 */
public enum ExperienceLevel {
    NOT_REQUIRED,
    INTERN,
    JUNIOR,
    MID,
    SENIOR,
    LEAD,
}
