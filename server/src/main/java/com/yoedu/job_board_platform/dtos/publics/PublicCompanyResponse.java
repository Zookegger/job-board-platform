package com.yoedu.job_board_platform.dtos.publics;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCompanyResponse {

    private UUID id;

    private String companyName;

    private String slug;

    private String logoUrl;

    private String description;

    private String website;

    private String email;

    private String phone;

    private String address;

    private String taxCode;

    private OffsetDateTime createdAt;

    private Long totalOpenJobs;
}