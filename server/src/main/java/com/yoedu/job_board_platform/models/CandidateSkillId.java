package com.yoedu.job_board_platform.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Khóa phức hợp cho CandidateSkill, gồm candidateId và skillId.
 */
@Embeddable
@Data
public class CandidateSkillId implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID candidateId;
    private Integer skillId;
}
