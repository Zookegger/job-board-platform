-- V17: Add updated_at to users and convert enum columns to VARCHAR
-- for JPA @Enumerated(EnumType.STRING) compatibility

-- 1. Add updated_at to users
ALTER TABLE users
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 2. Convert all enum columns to VARCHAR(255) in place (preserves data)

ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(255) USING role::text;

ALTER TABLE jobs
    ALTER COLUMN employment_type TYPE VARCHAR(255) USING employment_type::text,
    ALTER COLUMN experience_level TYPE VARCHAR(255) USING experience_level::text,
    ALTER COLUMN location_types TYPE VARCHAR(255) USING location_types::text,
    ALTER COLUMN status TYPE VARCHAR(255) USING status::text;

ALTER TABLE company_approval_logs
    ALTER COLUMN old_status TYPE VARCHAR(255) USING old_status::text,
    ALTER COLUMN new_status TYPE VARCHAR(255) USING new_status::text;

ALTER TABLE candidate_skills
    ALTER COLUMN proficient_level TYPE VARCHAR(255) USING proficient_level::text;

ALTER TABLE reports
    ALTER COLUMN reason TYPE VARCHAR(255) USING reason::text,
    ALTER COLUMN status TYPE VARCHAR(255) USING status::text;

ALTER TABLE companies
    ALTER COLUMN review_reason TYPE VARCHAR(255) USING review_reason::text,
    ALTER COLUMN status TYPE VARCHAR(255) USING status::text;

ALTER TABLE applications
    ALTER COLUMN status TYPE VARCHAR(255) USING status::text;

ALTER TABLE application_status_logs
    ALTER COLUMN status TYPE VARCHAR(255) USING status::text;

ALTER TABLE notifications
    ALTER COLUMN type TYPE VARCHAR(255) USING type::text;

-- 3. Handle reports.reported_by: set NOT NULL to match JPA @ManyToOne(optional = false)
UPDATE reports
SET reported_by = (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1)
WHERE reported_by IS NULL;

ALTER TABLE reports
    ALTER COLUMN reported_by SET NOT NULL;
