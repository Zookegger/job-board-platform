package com.yoedu.job_board_platform.tasks;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.repositories.JobRepository;

@ExtendWith(MockitoExtension.class)
class JobExpirationSchedulerTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobExpirationScheduler scheduler;

    // ----------------------------------------------------------------
    // US-27 TC-01: Khi có job ACTIVE đã quá hạn → gọi bulk update
    // ----------------------------------------------------------------
    @Test
    void expireOverdueJobs_callsRepositoryWithCorrectStatuses() {
        when(jobRepository.expireActiveJobs(eq(JobStatus.ACTIVE), eq(JobStatus.EXPIRED), any(OffsetDateTime.class)))
                .thenReturn(3);

        scheduler.expireOverdueJobs();

        ArgumentCaptor<OffsetDateTime> timeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(jobRepository).expireActiveJobs(
                eq(JobStatus.ACTIVE),
                eq(JobStatus.EXPIRED),
                timeCaptor.capture());

        // Thời điểm truyền vào phải là "bây giờ" (trong vòng 5 giây)
        OffsetDateTime now = OffsetDateTime.now();
        assertThat(timeCaptor.getValue()).isBefore(now.plusSeconds(5));
        assertThat(timeCaptor.getValue()).isAfter(now.minusSeconds(5));
    }

    // ----------------------------------------------------------------
    // US-27 TC-02: Khi không có job nào hết hạn → bulk update trả về 0
    // ----------------------------------------------------------------
    @Test
    void expireOverdueJobs_zeroExpired_noException() {
        when(jobRepository.expireActiveJobs(eq(JobStatus.ACTIVE), eq(JobStatus.EXPIRED), any(OffsetDateTime.class)))
                .thenReturn(0);

        // Không ném ngoại lệ khi không có gì để expire
        scheduler.expireOverdueJobs();

        verify(jobRepository).expireActiveJobs(any(), any(), any());
    }

    // ----------------------------------------------------------------
    // US-27 TC-03: Kiểm tra trạng thái nguồn là ACTIVE (không expire DRAFT, v.v.)
    // ----------------------------------------------------------------
    @Test
    void expireOverdueJobs_onlyTargetsActiveStatus() {
        when(jobRepository.expireActiveJobs(any(), any(), any())).thenReturn(0);

        scheduler.expireOverdueJobs();

        ArgumentCaptor<JobStatus> fromCaptor = ArgumentCaptor.forClass(JobStatus.class);
        ArgumentCaptor<JobStatus> toCaptor = ArgumentCaptor.forClass(JobStatus.class);
        verify(jobRepository).expireActiveJobs(fromCaptor.capture(), toCaptor.capture(), any());

        assertThat(fromCaptor.getValue()).isEqualTo(JobStatus.ACTIVE);
        assertThat(toCaptor.getValue()).isEqualTo(JobStatus.EXPIRED);
    }
}
