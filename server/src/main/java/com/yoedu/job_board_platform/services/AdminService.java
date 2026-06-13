package com.yoedu.job_board_platform.services;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.admin.PendingCompanyResponse;

public interface AdminService {
    Page<PendingCompanyResponse> getPendingCompanies(
            int page,
            int size,
            String keyword,
            Boolean hasTaxCode,
            Boolean hasContact,
            String sortBy,
            String direction);

    /**
     * Approves a registered company and marks it public.
     *
     * @param companyId company ID to approve
     * @throws ResourceNotFoundException when the company does not exist
     */
    void approveCompany(UUID companyId);

    /**
     * Rejects a registered company with an admin-provided reason.
     *
     * @param companyId company ID to reject
     * @param reason rejection reason
     * @throws ResourceNotFoundException when the company does not exist
     */
    void rejectCompany(UUID companyId, String reason);
}
