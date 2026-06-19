package com.yoedu.job_board_platform.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.SkillApi;
import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;
import com.yoedu.job_board_platform.mappers.SkillMapper;
import com.yoedu.job_board_platform.services.SkillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE + "/skills")
public class SkillController implements SkillApi {

    private final SkillService skillService;
    private final SkillMapper skillMapper;

    /** Lấy toàn bộ danh sách kỹ năng có sẵn (không cần đăng nhập). */
    @GetMapping
    @Override
    public ResponseEntity<Page<SkillResponse>> getAllSkills(Pageable pageable,
            SkillFilterRequest request) {
        SkillFilterRequest publicFilter = new SkillFilterRequest(request.keyword(), true);

        return ResponseEntity.ok(skillService.getAllSkills(pageable, publicFilter).map(skillMapper::toResponse));
    }

    /** Lấy danh sách kỹ năng của ứng viên đang đăng nhập. */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<CandidateSkillResponse>> getCandidateSkills() {
        return ResponseEntity.ok(skillService.getCandidateSkills());
    }

    /** Cập nhật toàn bộ kỹ năng của ứng viên đang đăng nhập (replace). */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<CandidateSkillResponse>> updateCandidateSkills(
            @Valid @RequestBody UpdateCandidateSkillsRequest request) {
        return ResponseEntity.ok(skillService.updateCandidateSkills(request));
    }
}
