package com.yoedu.job_board_platform.events;

import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;

public record JobStatusChangeEvent(Job job, JobStatus newStatus) {
}
