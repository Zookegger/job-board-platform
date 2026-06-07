package com.yoedu.job_board_platform.services;

import java.util.UUID;

public interface AdminService {
    void approveCompany(UUID companyId);
    void rejectCompany(UUID companyId);
}
