package com.yoedu.job_board_platform.models;

/**
 * Enum đại diện cho vai trò của người dùng trong hệ thống. Có ba vai trò chính:
 * - <strong>Admin:</strong> Quản trị viên có quyền quản lý toàn bộ hệ thống, bao gồm quản lý người dùng, công việc, và các chức năng khác.
 * - <strong>Employer:</strong> Nhà tuyển dụng có quyền đăng tuyển công việc, quản lý các công việc đã đăng, và xem các ứng viên đã ứng tuyển.
 * - <strong>Candidate:</strong> Ứng viên có quyền tìm kiếm công việc, ứng tuyển vào các công việc, và quản lý hồ sơ cá nhân của mình.
 */
public enum UserRole {
    ADMIN, EMPLOYER, CANDIDATE
}
