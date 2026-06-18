package com.yoedu.job_board_platform.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillRequest;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;
import com.yoedu.job_board_platform.models.Skill;

/**
 * Service quản lý kỹ năng (Skills).
 * <p>
 * Bao gồm:
 * <ul>
 *   <li>Tra cứu danh sách kỹ năng có sẵn (phân trang, lọc theo từ khóa + trạng thái)</li>
 *   <li>Quản lý kỹ năng của ứng viên (xem, thay thế toàn bộ)</li>
 *   <li>Quản trị CRUD kỹ năng (tạo, sửa, toggle trạng thái, xóa) — dành cho ADMIN</li>
 * </ul>
 */
public interface SkillService {

    /**
     * Lấy danh sách kỹ năng có phân trang, hỗ trợ lọc theo từ khóa tên và trạng thái.
     *
     * @param pageable thông tin phân trang
     * @param request  bộ lọc (keyword, isActive)
     * @return trang kết quả {@link SkillResponse}
     */
    Page<Skill> getAllSkills(Pageable pageable, SkillFilterRequest request);

    /** Lấy danh sách kỹ năng hiện tại của ứng viên đang đăng nhập. */
    List<CandidateSkillResponse> getCandidateSkills();

    /**
     * Cập nhật toàn bộ danh sách kỹ năng của ứng viên đang đăng nhập.
     * Xoá tất cả kỹ năng cũ và lưu danh sách mới (replace).
     *
     * @param request danh sách kỹ năng mới (skillId + proficientLevel)
     * @return danh sách kỹ năng đã lưu
     */
    List<CandidateSkillResponse> updateCandidateSkills(UpdateCandidateSkillsRequest request);

    /**
     * Tạo kỹ năng mới.
     * Tên kỹ năng phải chưa tồn tại, nếu không sẽ ném {@code ConflictException}.
     *
     * @param request thông tin kỹ năng (name bắt buộc, isActive tùy chọn)
     * @return kỹ năng đã tạo
     */
    Skill createSkill(SkillRequest request);

    /**
     * Cập nhật thông tin kỹ năng.
     * Kiểm tra trùng tên nếu tên được thay đổi.
     *
     * @param id      ID kỹ năng cần sửa
     * @param request thông tin mới
     * @return kỹ năng đã cập nhật
     */
    Skill updateSkill(Integer id, SkillRequest request);

    /**
     * Bật/tắt trạng thái hoạt động của kỹ năng (đảo ngược isActive).
     * Kỹ năng bị tắt sẽ không hiển thị trong danh sách lựa chọn cho ứng viên.
     *
     * @param id ID kỹ năng
     * @return kỹ năng với trạng thái mới
     */
    Skill toggleSkillActive(Integer id);

    /**
     * Xóa kỹ năng khỏi hệ thống.
     * Các bản ghi liên quan trong {@code job_skills} và {@code candidate_skills} cũng bị xóa theo.
     *
     * @param id ID kỹ năng cần xóa
     */
    void deleteSkill(Integer id);
}
