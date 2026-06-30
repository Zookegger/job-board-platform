package com.yoedu.job_board_platform.specifications;

import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import jakarta.persistence.criteria.JoinType;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public final class UserSpecification {

	public static Specification<User> hasKeyword(String keyword) {
		return ((root, query, cb) -> {
			if (keyword == null || keyword.isBlank()) {
				return cb.conjunction();
			}
			String pattern = "%" + keyword.trim().toLowerCase() + "%";
			var profileJoin = root.join("profiles", JoinType.INNER);
			return cb.or(
					cb.equal(root.get("id"), profileJoin.get("id")),
					cb.like(cb.lower(root.get("email")), pattern),
					cb.like(cb.lower(profileJoin.get("fullName")), pattern),
					cb.like(cb.lower(profileJoin.get("phone")), pattern)
			);
		});
	}

	public static Specification<User> hasRole(UserRole role) {
		return (root, query, cb) -> {
			if (role == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get("role"), role);
		};
	}

	public static Specification<User> isActive(Boolean isActive) {
		return (root, query, cb) -> {
			if (isActive == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get("isActive"), isActive);
		};
	}
}