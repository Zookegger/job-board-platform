package com.yoedu.job_board_platform.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class UserSpecification {

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