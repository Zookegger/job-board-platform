package com.yoedu.job_board_platform.events;

import java.util.UUID;

public record NewCompanyUserEvent(UUID companyId, String companyName) {
}
