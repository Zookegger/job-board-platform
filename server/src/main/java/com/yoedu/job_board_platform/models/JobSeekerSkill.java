package com.yoedu.job_board_platform.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_seeker_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerSkill {
    @EmbeddedId
    private JobSeekerSkillId id;
}
