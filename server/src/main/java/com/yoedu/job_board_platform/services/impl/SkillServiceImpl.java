package com.yoedu.job_board_platform.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;
import com.yoedu.job_board_platform.models.CandidateSkill;
import com.yoedu.job_board_platform.models.CandidateSkillId;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.services.SkillService;
import com.yoedu.job_board_platform.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final SecurityUtil securityUtil;

    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName()))
                .toList();
    }

    @Override
    public List<CandidateSkillResponse> getCandidateSkills() {
        UUID candidateId = requireCandidate();
        return toResponseList(candidateSkillRepository.findAllByIdCandidateId(candidateId));
    }

    @Override
    @Transactional
    public List<CandidateSkillResponse> updateCandidateSkills(UpdateCandidateSkillsRequest request) {
        UUID candidateId = requireCandidate();

        // Validate all skill IDs exist
        List<Integer> skillIds = request.skills().stream()
                .map(UpdateCandidateSkillsRequest.CandidateSkillItem::skillId)
                .distinct()
                .toList();

        List<Skill> skills = skillRepository.findAllById(skillIds);
        if (skills.size() != skillIds.size()) {
            throw new BadRequestException("Một hoặc nhiều kỹ năng không tồn tại trong hệ thống");
        }

        // Replace all existing skills
        candidateSkillRepository.deleteAllByIdCandidateId(candidateId);

        List<CandidateSkill> newSkills = request.skills().stream()
                .map(item -> {
                    CandidateSkillId id = new CandidateSkillId();
                    id.setCandidateId(candidateId);
                    id.setSkillId(item.skillId());
                    return CandidateSkill.builder()
                            .id(id)
                            .proficientLevel(item.proficientLevel())
                            .build();
                })
                .toList();

        candidateSkillRepository.saveAll(newSkills);
        return toResponseList(newSkills);
    }

    // ----------------------------------------------------------------

    private UUID requireCandidate() {
        User user = securityUtil.getCurrentUser();
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new ForbiddenException("Chỉ ứng viên mới có thể thao tác với kỹ năng");
        }
        return user.getId();
    }

    private List<CandidateSkillResponse> toResponseList(List<CandidateSkill> candidateSkills) {
        // Build a map for skill names (batch fetch)
        List<Integer> ids = candidateSkills.stream().map(cs -> cs.getId().getSkillId()).toList();
        var skillMap = skillRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Skill::getId, Skill::getName));

        return candidateSkills.stream()
                .map(cs -> new CandidateSkillResponse(
                        cs.getId().getSkillId(),
                        skillMap.getOrDefault(cs.getId().getSkillId(), ""),
                        cs.getProficientLevel()))
                .toList();
    }
}
