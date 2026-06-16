package com.yoedu.job_board_platform.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.yoedu.job_board_platform.dtos.skill.SkillResponse;

/**
 * Service quản lý kỹ năng của tin tuyển dụng.
 * Xử lý đồng bộ danh sách kỹ năng và truy vấn kỹ năng theo tin tuyển dụng.
 */
public interface JobSkillService {

    /**
     * Đồng bộ danh sách kỹ năng cho một tin tuyển dụng.
     * Xoá toàn bộ kỹ năng cũ và thay thế bằng danh sách mới.
     *
     * @param jobId    UUID của tin tuyển dụng
     * @param skillIds danh sách ID kỹ năng mới (có thể null hoặc rỗng để xoá hết)
     */
    void syncJobSkills(UUID jobId, Set<Integer> skillIds);

    /**
     * Lấy danh sách kỹ năng của một tin tuyển dụng.
     *
     * @param jobId UUID của tin tuyển dụng
     * @return danh sách SkillResponse (rỗng nếu không có kỹ năng nào)
     */
    List<SkillResponse> getSkillsByJobId(UUID jobId);
}
