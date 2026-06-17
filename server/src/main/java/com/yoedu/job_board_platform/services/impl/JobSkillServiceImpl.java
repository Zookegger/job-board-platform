package com.yoedu.job_board_platform.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.models.JobSkill;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.services.JobSkillService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobSkillServiceImpl implements JobSkillService {

    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public void syncJobSkills(UUID jobId, Set<Integer> requestedSkillIds) {
        Set<Integer> incomingIds = requestedSkillIds != null ? requestedSkillIds : Set.of();

        // 1. Fetch current IDs directly via a custom lightweight query
        Set<Integer> currentSkillIds = jobSkillRepository.findSkillIdsByJobId(jobId);

        // 2. Identify Deletes (Exist in DB, missing in Request)
        Set<Integer> idsToDelete = new HashSet<>(currentSkillIds);
        idsToDelete.removeAll(incomingIds);
        if (!idsToDelete.isEmpty()) {
            jobSkillRepository.deleteByJobIdAndSkillIdIn(jobId, idsToDelete);
        }

        // 3. Identify Inserts (Exist in Request, missing in DB)
        Set<Integer> idsToInsert = new HashSet<>(incomingIds);
        idsToInsert.removeAll(currentSkillIds);
        if (!idsToInsert.isEmpty()) {
            List<JobSkill> newSkills = idsToInsert.stream()
                    .map(skillId -> new JobSkill(jobId, skillId))
                    .toList();
            jobSkillRepository.saveAll(newSkills);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getSkillsByJobId(UUID jobId) {
        List<Integer> skillIds = jobSkillRepository.findAllByJobId(jobId)
                .stream()
                .map(JobSkill::getSkillId)
                .toList();

        return skillRepository.findAllById(skillIds)
                .stream()
                .map(skill -> new SkillResponse(skill.getId(), skill.getName(), skill.isActive()))
                .toList();
    }
}
