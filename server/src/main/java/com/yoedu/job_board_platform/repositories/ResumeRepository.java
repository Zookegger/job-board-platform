package com.yoedu.job_board_platform.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoedu.job_board_platform.models.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    Optional<Resume> findByCandidateDetailProfileId(UUID candidateDetailId);
    void deleteByCandidateDetailProfileId(UUID candidateDetailId);
}
