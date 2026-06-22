package com.yoedu.job_board_platform.dtos.company;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicCompanyListResponse(
        String companyName,
        String slug,
        String logoUrl,
        String description,
        String address,
        String website,
        Long totalOpenJobs) {
}
