package com.yoedu.job_board_platform.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.CandidateSkill;
import com.yoedu.job_board_platform.models.CandidateSkillId;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, CandidateSkillId> {
    List<CandidateSkill> findAllByIdCandidateId(UUID candidateId);

    void deleteAllByIdCandidateId(UUID candidateId);

    @Modifying
    @Query("DELETE FROM CandidateSkill cs WHERE cs.id.skillId = :skillId")
    void deleteAllBySkillId(@Param("skillId") Integer skillId);
}
