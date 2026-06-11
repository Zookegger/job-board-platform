package com.yoedu.job_board_platform.models;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_skills")
@IdClass(JobSkillId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Liên kết giữa Job và Skill (n-n).
 * Sử dụng IdClass JobSkillId làm khóa phức hợp.
 */
public class JobSkill {
    @Id
    private UUID jobId;

    @Id
    private Integer skillId;
}
