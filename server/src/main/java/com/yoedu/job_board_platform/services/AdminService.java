package com.yoedu.job_board_platform.services;

import java.util.UUID;

/**
 * Service quản trị hệ thống.
 * Xử lý phê duyệt và từ chối công ty đăng ký.
 */
public interface AdminService {
    void approveCompany(UUID companyId);
    void rejectCompany(UUID companyId);
}
