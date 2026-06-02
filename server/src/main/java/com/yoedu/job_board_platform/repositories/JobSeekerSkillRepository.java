package com.yoedu.job_board_platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.JobSeekerSkill;
import com.yoedu.job_board_platform.models.JobSeekerSkillId;

@Repository
public interface JobSeekerSkillRepository extends JpaRepository<JobSeekerSkill, JobSeekerSkillId> {
}
