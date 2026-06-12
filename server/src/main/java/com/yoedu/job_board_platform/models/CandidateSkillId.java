package com.yoedu.job_board_platform.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
/**
 * Khóa phức hợp cho CandidateSkill, gồm candidateId và skillId.
 */
public class CandidateSkillId implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID candidateId;
    private Integer skillId;
}
