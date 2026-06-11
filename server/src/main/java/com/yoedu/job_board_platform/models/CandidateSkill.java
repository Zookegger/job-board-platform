package com.yoedu.job_board_platform.models;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Kỹ năng của ứng viên, sử dụng khóa phức hợp CandidateSkillId.
 * Liên kết CandidateDetail với Skill và lưu mức độ thành thạo (ProficientLevel).
 */
public class CandidateSkill {
    @EmbeddedId
    private CandidateSkillId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficient_level", nullable = false)
    private ProficientLevel proficientLevel;
}
