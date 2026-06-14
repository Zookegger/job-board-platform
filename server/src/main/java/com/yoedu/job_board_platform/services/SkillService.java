package com.yoedu.job_board_platform.services;

import java.util.List;

import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;

/**
 * Service quản lý kỹ năng.
 * Hỗ trợ lấy danh sách kỹ năng có sẵn và quản lý kỹ năng của ứng viên.
 */
public interface SkillService {

    /** Lấy toàn bộ danh sách kỹ năng có sẵn trong hệ thống. */
    List<SkillResponse> getAllSkills();

    /** Lấy danh sách kỹ năng hiện tại của ứng viên đang đăng nhập. */
    List<CandidateSkillResponse> getCandidateSkills();

    /**
     * Cập nhật toàn bộ danh sách kỹ năng của ứng viên đang đăng nhập.
     * Xoá tất cả kỹ năng cũ và lưu danh sách mới.
     */
    List<CandidateSkillResponse> updateCandidateSkills(UpdateCandidateSkillsRequest request);
}
