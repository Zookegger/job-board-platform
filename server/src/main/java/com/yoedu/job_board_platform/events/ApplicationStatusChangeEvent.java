package com.yoedu.job_board_platform.events;

import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;

public record ApplicationStatusChangeEvent(Application application, ApplicationStatus newStatus) {
}
