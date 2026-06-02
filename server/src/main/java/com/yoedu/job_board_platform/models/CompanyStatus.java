package com.yoedu.job_board_platform.models;

/**
 * Trạng thái của công ty trên nền tảng
 * - <strong>Pending:</strong> Công ty đã đăng ký nhưng chưa được duyệt, không hiển thị công khai
 * - <strong>Approved:</strong> Công ty đã được duyệt, hiển thị công khai và có thể đăng tuyển dụng
 * - <strong>Rejected:</strong> Công ty bị từ chối duyệt, không hiển thị công khai
 * - <strong>Suspended:</strong> Công ty bị khóa/đình chỉ do vi phạm, không hiển thị công khai và không thể đăng tuyển dụng
 */
public enum CompanyStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}
