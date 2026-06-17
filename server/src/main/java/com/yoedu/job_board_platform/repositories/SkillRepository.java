package com.yoedu.job_board_platform.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Skill;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer>, JpaSpecificationExecutor<Skill> {
    Optional<Skill> findByName(String name);

    boolean existsByName(String name);
}
