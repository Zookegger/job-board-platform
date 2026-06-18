package com.yoedu.job_board_platform.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;

public final class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            try {
                JobStatus jobStatus = JobStatus.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), jobStatus);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }
}
