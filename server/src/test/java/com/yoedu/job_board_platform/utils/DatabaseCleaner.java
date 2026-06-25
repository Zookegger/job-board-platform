package com.yoedu.job_board_platform.utils;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseCleaner {

    private static final String TRUNCATE_SQL = """
        TRUNCATE TABLE
            application_status_logs,
            applications,
            candidate_skills,
            job_skills,
            jobs,
            company_approval_logs,
            company_employer_details,
            companies,
            resumes,
            refresh_tokens,
            profiles,
            users,
            candidate_details,
            job_category,
            skills,
            notifications,
            reports
        RESTART IDENTITY CASCADE
        """;

    public static void cleanAllTables(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        jdbcTemplate.execute(TRUNCATE_SQL);
        entityManager.clear();
    }
}
