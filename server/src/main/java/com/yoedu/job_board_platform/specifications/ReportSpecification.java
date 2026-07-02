package com.yoedu.job_board_platform.specifications;

import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.ReportReason;
import com.yoedu.job_board_platform.models.ReportStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ReportSpecification {
	public static Specification<Report> hasStatus(ReportStatus status) {
		return (root, query, cb) -> {
			if (status == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get("status"), status);
		};
	}

	public static Specification<Report> hasReason(ReportReason reason) {
		return (root, query, cb) -> {
			if (reason == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get("reason"), reason);
		};
	}
}
