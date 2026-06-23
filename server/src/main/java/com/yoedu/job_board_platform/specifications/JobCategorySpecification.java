package com.yoedu.job_board_platform.specifications;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.models.JobCategory;

public final class JobCategorySpecification {
    public static Specification<JobCategory> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            String normalizedKeyword = normalizeKeyword(keyword);

            if (normalizedKeyword == null) {
                return cb.conjunction();
            }

            String pattern = "%" + normalizedKeyword + "%";

            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim().toLowerCase(Locale.ROOT);
    }
}
