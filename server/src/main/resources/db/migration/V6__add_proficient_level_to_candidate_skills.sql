CREATE TYPE proficient_level AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT');

ALTER TABLE candidate_skills ADD COLUMN proficient_level proficient_level NOT NULL DEFAULT 'BEGINNER';
COMMENT ON COLUMN candidate_skills.proficient_level IS 'Mức độ thành thạo kỹ năng';
