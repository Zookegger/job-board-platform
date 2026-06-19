package com.yoedu.job_board_platform.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.models.Skill;

/**
 * Tiêu chí tìm kiếm động (Specification) cho thực thể Skill.
 * Hỗ trợ lọc theo từ khóa tên kỹ năng và trạng thái hoạt động.
 */
public class SkillSpecification {

    /**
     * Tìm kỹ năng theo từ khóa tên (không phân biệt hoa-thường, LIKE %keyword%).
     *
     * @param keyword từ khóa tìm kiếm (có thể null/blank)
     * @return Specification để lọc theo tên, hoặc null nếu keyword rỗng
     */
    public static Specification<Skill> withKeyword(String keyword) {
        if (keyword == null || keyword.isBlank())
            return Specification.unrestricted();
        String pattern = "%" + keyword.toLowerCase() + "%";

        return (root, query, cb) -> {
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    /**
     * Lọc kỹ năng theo trạng thái hoạt động (isActive).
     *
     * @param isActive true = đang hoạt động, false = đã tắt, null = bỏ qua
     * @return Specification để lọc theo trạng thái, hoặc null nếu isActive null
     */
    public static Specification<Skill> hasStatus(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
