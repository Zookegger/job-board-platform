package com.yoedu.job_board_platform.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "candidate_details")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateDetail {
    @Id
    @Column(name = "profile_id", columnDefinition = "uuid")
    private UUID profileId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", insertable = false, updatable = false)
    private Profile profile;

    @Column(name = "cv_file_url", unique = true, columnDefinition = "text")
    private String cvFileUrl;
}
