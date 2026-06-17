package com.yoedu.job_board_platform.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.JobSkill;
import com.yoedu.job_board_platform.models.JobSkillId;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {
    List<JobSkill> findAllByJobId(UUID jobId);

    void deleteAllByJobId(UUID jobId);

    @Modifying
    @Query("DELETE FROM JobSkill js WHERE js.id.skillId = :skillId")
    void deleteAllBySkillId(@Param("skillId") Integer skillId);

    void deleteByJobIdAndSkillIdIn(UUID jobId, Collection<Integer> skillIds);

    @Query("SELECT js.skillId FROM JobSkill js WHERE js.jobId = :jobId")
    Set<Integer> findSkillIdsByJobId(@Param("jobId") UUID jobId);
}
