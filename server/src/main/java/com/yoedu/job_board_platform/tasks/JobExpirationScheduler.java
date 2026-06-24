package com.yoedu.job_board_platform.tasks;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.repositories.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled task tự động chuyển các tin tuyển dụng ACTIVE đã quá hạn
 * (expirationDate < now) sang trạng thái EXPIRED.
 * Chạy mỗi giờ một lần.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobExpirationScheduler {

    private final JobRepository jobRepository;

    @Scheduled(fixedRate = 3600000) // mỗi 1 giờ
    @Transactional
    public void expireOverdueJobs() {
        OffsetDateTime now = OffsetDateTime.now();
        int count = jobRepository.expireActiveJobs(JobStatus.ACTIVE, JobStatus.EXPIRED, now);
        if (count > 0) {
            log.info("JobExpirationScheduler: đã chuyển {} tin tuyển dụng sang EXPIRED", count);
        }
    }
}
