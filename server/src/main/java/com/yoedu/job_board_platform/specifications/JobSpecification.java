package com.yoedu.job_board_platform.specifications;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;

import jakarta.persistence.criteria.Subquery;

public final class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            try {
                JobStatus jobStatus = JobStatus.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), jobStatus);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Job> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            var companyJoin = root.join("company");
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(companyJoin.get("companyName")), pattern));
        };
    }

    public static Specification<Job> hasCategoryIds(Set<Integer> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Job> hasLocationTypes(Set<LocationTypes> locationTypes) {
        return (root, query, cb) -> {
            if (locationTypes == null || locationTypes.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("locationTypes").in(locationTypes);
        };
    }

    public static Specification<Job> hasEmploymentTypes(Set<EmploymentType> employmentTypes) {
        return (root, query, cb) -> {
            if (employmentTypes == null || employmentTypes.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("employmentType").in(employmentTypes);
        };
    }

    public static Specification<Job> hasExperienceLevels(Set<ExperienceLevel> experienceLevels) {
        return (root, query, cb) -> {
            if (experienceLevels == null || experienceLevels.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("experienceLevel").in(experienceLevels);
        };
    }

    public static Specification<Job> hasNotExpired() {
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("expirationDate")),
                        cb.greaterThanOrEqualTo(root.get("expirationDate"), OffsetDateTime.now()));
    }

    public static Specification<Job> salaryOverlap(BigDecimal minSalary, BigDecimal maxSalary) {
        return (root, query, cb) -> {
            if (minSalary == null && maxSalary == null) {
                return cb.conjunction();
            }
            if (minSalary != null && maxSalary != null) {
                return cb.and(
                        cb.greaterThanOrEqualTo(
                                cb.coalesce(root.get("salaryMin"), BigDecimal.ZERO),
                                minSalary),
                        cb.lessThanOrEqualTo(
                                cb.coalesce(root.get("salaryMax"), maxSalary),
                                maxSalary));
            }
            if (minSalary != null) {
                return cb.greaterThanOrEqualTo(
                        cb.coalesce(root.get("salaryMin"), BigDecimal.ZERO),
                        minSalary);
            }
            return cb.lessThanOrEqualTo(
                    cb.coalesce(root.get("salaryMax"), maxSalary),
                    maxSalary);
        };
    }

    public static Specification<Job> hasSkillIds(Set<Integer> skillIds) {
        return (root, query, cb) -> {
            if (skillIds == null || skillIds.isEmpty()) {
                return cb.conjunction();
            }
            Subquery<Long> subquery = query.subquery(Long.class);
            var jobSkill = subquery.from(com.yoedu.job_board_platform.models.JobSkill.class);
            subquery.select(jobSkill.get("jobId"))
                    .where(jobSkill.get("skillId").in(skillIds))
                    .groupBy(jobSkill.get("jobId"))
                    .having(cb.equal(cb.count(jobSkill.get("skillId")), (long) skillIds.size()));
            return root.get("id").in(subquery);
        };
    }
}
