package com.yoedu.job_board_platform.events;

import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;

public record CompanyStatusChangeEvent(Company company, CompanyStatus newStatus) {
}
