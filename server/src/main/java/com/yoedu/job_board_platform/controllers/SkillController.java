package com.yoedu.job_board_platform.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;
import com.yoedu.job_board_platform.services.SkillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /** Lấy toàn bộ danh sách kỹ năng có sẵn (không cần đăng nhập). */
    @GetMapping("/api/skills")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    /** Lấy danh sách kỹ năng của ứng viên đang đăng nhập. */
    @GetMapping("/api/profile/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<CandidateSkillResponse>> getCandidateSkills() {
        return ResponseEntity.ok(skillService.getCandidateSkills());
    }

    /** Cập nhật toàn bộ kỹ năng của ứng viên đang đăng nhập (replace). */
    @PutMapping("/api/profile/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<CandidateSkillResponse>> updateCandidateSkills(
            @Valid @RequestBody UpdateCandidateSkillsRequest request) {
        return ResponseEntity.ok(skillService.updateCandidateSkills(request));
    }
}
