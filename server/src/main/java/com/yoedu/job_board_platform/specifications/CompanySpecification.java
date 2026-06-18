package com.yoedu.job_board_platform.specifications;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class CompanySpecification {

    private CompanySpecification() {
    }

    public static Specification<Company> isPending() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), CompanyStatus.PENDING);
    }

    public static Specification<Company> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            String normalizedKeyword = normalizeKeyword(keyword);

            if (normalizedKeyword == null) {
                return cb.conjunction();
            }

            String pattern = "%" + normalizedKeyword + "%";

            return cb.or(
                    like(cb, root, "companyName", pattern),
                    like(cb, root, "email", pattern),
                    like(cb, root, "phone", pattern),
                    like(cb, root, "taxCode", pattern),
                    like(cb, root, "address", pattern),
                    like(cb, root, "website", pattern)
            );
        };
    }

    public static Specification<Company> hasTaxCode(Boolean hasTaxCode) {
        return (root, query, cb) -> {
            if (hasTaxCode == null) {
                return cb.conjunction();
            }

            return hasTaxCode
                    ? hasValue(cb, root, "taxCode")
                    : missingValue(cb, root, "taxCode");
        };
    }

    public static Specification<Company> hasContact(Boolean hasContact) {
        return (root, query, cb) -> {
            if (hasContact == null) {
                return cb.conjunction();
            }

            Predicate hasEmail = hasValue(cb, root, "email");
            Predicate hasPhone = hasValue(cb, root, "phone");

            return hasContact
                    ? cb.or(hasEmail, hasPhone)
                    : cb.and(
                            missingValue(cb, root, "email"),
                            missingValue(cb, root, "phone")
                    );
        };
    }

    private static Predicate like(
            CriteriaBuilder cb,
            Root<Company> root,
            String field,
            String pattern
    ) {
        return cb.like(cb.lower(root.<String>get(field)), pattern);
    }

    private static Predicate hasValue(
            CriteriaBuilder cb,
            Root<Company> root,
            String field
    ) {
        return cb.and(
                cb.isNotNull(root.get(field)),
                cb.notEqual(cb.trim(root.<String>get(field)), "")
        );
    }

    private static Predicate missingValue(
            CriteriaBuilder cb,
            Root<Company> root,
            String field
    ) {
        return cb.or(
                cb.isNull(root.get(field)),
                cb.equal(cb.trim(root.<String>get(field)), "")
        );
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim().toLowerCase(Locale.ROOT);
    }
}