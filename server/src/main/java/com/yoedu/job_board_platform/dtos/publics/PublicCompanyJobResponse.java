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
public class PublicCompanyJobResponse {

    private UUID id;

    private String title;

    private String location;

    private String status;

    private OffsetDateTime createdAt;
}