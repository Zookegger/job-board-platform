package com.yoedu.job_board_platform.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ConflictException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.services.impl.SkillServiceImpl;
import com.yoedu.job_board_platform.dtos.skill.SkillRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;
import com.yoedu.job_board_platform.mappers.SkillMapper;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.utils.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private CandidateSkillRepository candidateSkillRepository;

    @Mock
    private JobSkillRepository jobSkillRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    private Skill activeSkill;
    private Skill inactiveSkill;
    private SkillResponse activeResponse;
    private SkillResponse inactiveResponse;

    @BeforeEach
    void setUp() {
        activeSkill = Skill.builder()
                .id(1)
                .name("Java")
                .isActive(true)
                .build();
        inactiveSkill = Skill.builder()
                .id(2)
                .name("Kotlin")
                .isActive(false)
                .build();
        activeResponse = new SkillResponse(1, "Java", true);
        inactiveResponse = new SkillResponse(2, "Kotlin", false);
    }

    // ----------------------------------------------------------------
    // getAllSkills — public, active only
    // ----------------------------------------------------------------

    @Test
    void getAllSkills_returnsOnlyActive() {
        Page<Skill> skillPage = new PageImpl<>(List.of(activeSkill));
        when(skillRepository.findAll(ArgumentMatchers.<Specification<Skill>>any(), any(Pageable.class)))
                .thenReturn(skillPage);
        when(skillMapper.toResponse(activeSkill)).thenReturn(activeResponse);

        Page<SkillResponse> result = skillService.getAllSkills(Pageable.unpaged(), new SkillFilterRequest(null, true));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Java");
        assertThat(result.getContent().getFirst().isActive()).isTrue();
        verify(skillMapper).toResponse(activeSkill);
        verify(skillMapper, never()).toResponse(inactiveSkill);
    }

    @Test
    void getAllSkills_emptyWhenNoActive() {
        when(skillRepository.findAll(ArgumentMatchers.<Specification<Skill>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<SkillResponse> result = skillService.getAllSkills(Pageable.unpaged(), new SkillFilterRequest(null, true));

        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // getAllSkills — admin, all skills regardless of active
    // ----------------------------------------------------------------

    @Test
    void getAllSkills_admin_returnsAllSkills() {
        Page<Skill> skillPage = new PageImpl<>(List.of(activeSkill, inactiveSkill));
        when(skillRepository.findAll(ArgumentMatchers.<Specification<Skill>>any(), any(Pageable.class)))
                .thenReturn(skillPage);
        when(skillMapper.toResponse(activeSkill)).thenReturn(activeResponse);
        when(skillMapper.toResponse(inactiveSkill)).thenReturn(inactiveResponse);

        Page<SkillResponse> result = skillService.getAllSkills(Pageable.unpaged(), new SkillFilterRequest(null, null));

        assertThat(result.getContent()).hasSize(2);
        verify(skillMapper, times(2)).toResponse(any(Skill.class));
    }

    // ----------------------------------------------------------------
    // createSkill
    // ----------------------------------------------------------------

    @Test
    void createSkill_success() {
        SkillRequest request = new SkillRequest("Rust", true);

        when(skillRepository.existsByName("Rust")).thenReturn(false);
        when(skillMapper.toEntity(request)).thenReturn(Skill.builder().name("Rust").isActive(true).build());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill s = invocation.getArgument(0);
            s.setId(3);
            return s;
        });
        when(skillMapper.toResponse(any(Skill.class)))
                .thenReturn(new SkillResponse(3, "Rust", true));

        SkillResponse result = skillService.createSkill(request);

        assertThat(result.name()).isEqualTo("Rust");
        assertThat(result.isActive()).isTrue();
        verify(skillRepository).existsByName("Rust");
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void createSkill_duplicateName_throwsConflict() {
        SkillRequest request = new SkillRequest("Java", true);

        when(skillRepository.existsByName("Java")).thenReturn(true);

        assertThatThrownBy(() -> skillService.createSkill(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Đã có kỹ năng");

        verify(skillRepository, never()).save(any(Skill.class));
    }

    // ----------------------------------------------------------------
    // updateSkill
    // ----------------------------------------------------------------

    @Test
    void updateSkill_success() {
        SkillRequest request = new SkillRequest("Java 8", true);

        when(skillRepository.findById(1)).thenReturn(Optional.of(activeSkill));
        when(skillRepository.findByName("Java 8")).thenReturn(Optional.empty());
        when(skillRepository.save(activeSkill)).thenReturn(activeSkill);
        when(skillMapper.toResponse(activeSkill)).thenReturn(new SkillResponse(1, "Java 8", true));

        SkillResponse result = skillService.updateSkill(1, request);

        assertThat(result.name()).isEqualTo("Java 8");
        verify(skillMapper).updateEntity(request, activeSkill);
    }

    @Test
    void updateSkill_nameConflictsWithOther_throwsConflict() {
        Skill other = Skill.builder().id(99).name("Java").isActive(true).build();
        SkillRequest request = new SkillRequest("Java", true);

        when(skillRepository.findById(1)).thenReturn(Optional.of(activeSkill));
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> skillService.updateSkill(1, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateSkill_notFound_throwsNotFound() {
        SkillRequest request = new SkillRequest("Go", true);

        when(skillRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.updateSkill(999, request))
                .isInstanceOf(NotFoundException.class);
    }

    // ----------------------------------------------------------------
    // toggleSkillActive
    // ----------------------------------------------------------------

    @Test
    void toggleSkillActive_flipsStatus() {
        when(skillRepository.findById(1)).thenReturn(Optional.of(activeSkill));
        when(skillRepository.save(activeSkill)).thenReturn(activeSkill);
        when(skillMapper.toResponse(activeSkill)).thenReturn(new SkillResponse(1, "Java", false));

        SkillResponse result = skillService.toggleSkillActive(1);

        assertThat(result.isActive()).isFalse();
        verify(skillRepository).save(activeSkill);
    }

    @Test
    void toggleSkillActive_togglesInactiveToActive() {
        when(skillRepository.findById(2)).thenReturn(Optional.of(inactiveSkill));
        when(skillRepository.save(inactiveSkill)).thenReturn(inactiveSkill);
        when(skillMapper.toResponse(inactiveSkill)).thenReturn(new SkillResponse(2, "Kotlin", true));

        SkillResponse result = skillService.toggleSkillActive(2);

        assertThat(result.isActive()).isTrue();
        verify(skillRepository).save(inactiveSkill);
    }

    @Test
    void toggleSkillActive_notFound_throwsNotFound() {
        when(skillRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.toggleSkillActive(999))
                .isInstanceOf(NotFoundException.class);
    }

    // ----------------------------------------------------------------
    // deleteSkill
    // ----------------------------------------------------------------

    @Test
    void deleteSkill_success() {
        when(skillRepository.existsById(1)).thenReturn(true);

        skillService.deleteSkill(1);

        verify(candidateSkillRepository).deleteAllBySkillId(1);
        verify(jobSkillRepository).deleteAllBySkillId(1);
        verify(skillRepository).deleteById(1);
    }

    @Test
    void deleteSkill_notFound_throwsNotFound() {
        when(skillRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> skillService.deleteSkill(999))
                .isInstanceOf(NotFoundException.class);

        verify(candidateSkillRepository, never()).deleteAllBySkillId(any());
        verify(jobSkillRepository, never()).deleteAllBySkillId(any());
        verify(skillRepository, never()).deleteById(any());
    }

    // ----------------------------------------------------------------
    // getCandidateSkills
    // ----------------------------------------------------------------

    @Test
    void getCandidateSkills_returnsSkillsForCandidate() {
        User candidate = User.builder().id(UUID.randomUUID()).role(UserRole.CANDIDATE).build();

        when(securityUtil.getCurrentUser()).thenReturn(candidate);

        skillService.getCandidateSkills();

        verify(candidateSkillRepository).findAllByIdCandidateId(candidate.getId());
    }

    @Test
    void getCandidateSkills_nonCandidate_throwsForbidden() {
        User employer = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYER).build();

        when(securityUtil.getCurrentUser()).thenReturn(employer);

        assertThatThrownBy(() -> skillService.getCandidateSkills())
                .isInstanceOf(ForbiddenException.class);
    }

    // ----------------------------------------------------------------
    // updateCandidateSkills
    // ----------------------------------------------------------------

    @Test
    void updateCandidateSkills_success() {
        User candidate = User.builder().id(UUID.randomUUID()).role(UserRole.CANDIDATE).build();
        var item = new UpdateCandidateSkillsRequest.CandidateSkillItem(1, null);
        UpdateCandidateSkillsRequest request = new UpdateCandidateSkillsRequest(List.of(item));

        when(securityUtil.getCurrentUser()).thenReturn(candidate);
        when(skillRepository.findAllById(List.of(1))).thenReturn(List.of(activeSkill));

        List<?> result = skillService.updateCandidateSkills(request);

        assertThat(result).isNotNull();
        verify(candidateSkillRepository).deleteAllByIdCandidateId(candidate.getId());
        verify(candidateSkillRepository).saveAll(any());
    }

    @Test
    void updateCandidateSkills_invalidSkillId_throwsBadRequest() {
        User candidate = User.builder().id(UUID.randomUUID()).role(UserRole.CANDIDATE).build();
        var item = new UpdateCandidateSkillsRequest.CandidateSkillItem(999, null);
        UpdateCandidateSkillsRequest request = new UpdateCandidateSkillsRequest(List.of(item));

        when(securityUtil.getCurrentUser()).thenReturn(candidate);
        when(skillRepository.findAllById(List.of(999))).thenReturn(List.of()); // not found

        assertThatThrownBy(() -> skillService.updateCandidateSkills(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không tồn tại");
    }

    @Test
    void updateCandidateSkills_nonCandidate_throwsForbidden() {
        User employer = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYER).build();

        when(securityUtil.getCurrentUser()).thenReturn(employer);

        assertThatThrownBy(() -> skillService.updateCandidateSkills(
                new UpdateCandidateSkillsRequest(List.of())))
                .isInstanceOf(ForbiddenException.class);
    }
}
