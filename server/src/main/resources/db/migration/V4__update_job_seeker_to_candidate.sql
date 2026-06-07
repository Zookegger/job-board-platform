-- Rename user_role enum value
ALTER TYPE user_role RENAME VALUE 'JOB_SEEKER' TO 'CANDIDATE';

-- Rename tables
ALTER TABLE job_seeker_details RENAME TO candidate_details;
ALTER TABLE job_seeker_skills RENAME TO candidate_skills;

-- Rename column in candidate_skills
ALTER TABLE candidate_skills RENAME COLUMN job_seeker_id TO candidate_id;

-- Update comments
COMMENT ON COLUMN candidate_details.cv_file_url IS 'Lưu trữ URL tới file PDF CV';
