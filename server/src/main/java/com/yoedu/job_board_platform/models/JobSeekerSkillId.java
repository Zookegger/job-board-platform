package com.yoedu.job_board_platform.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class JobSeekerSkillId implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID jobSeekerId;
    private Integer skillId;
}
