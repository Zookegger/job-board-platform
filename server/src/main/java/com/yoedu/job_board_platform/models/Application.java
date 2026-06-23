package com.yoedu.job_board_platform.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"candidate_id", "job_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Đại diện cho đơn ứng tuyển của một ứng viên vào một công việc.
 * Mỗi ứng viên chỉ có thể ứng tuyển một lần cho mỗi công việc (unique constraint trên candidate_id + job_id).
 */
public class Application {
    @Id
    @Column(columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Profile candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "cover_letter", columnDefinition = "text")
    private String coverLetter;

    @Column(name = "resume_url", columnDefinition = "text")
    private String resumeUrl;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt;
}
