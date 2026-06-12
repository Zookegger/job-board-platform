CREATE TABLE resumes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_detail_id uuid UNIQUE NOT NULL,
    title varchar(255) NOT NULL DEFAULT '',
    original_file_name varchar(255) NOT NULL DEFAULT '',
    file_path text NOT NULL,
    file_size bigint NOT NULL DEFAULT 0,
    file_type varchar(50) NOT NULL DEFAULT 'application/pdf',
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO resumes (candidate_detail_id, title, original_file_name, file_path, file_size, file_type)
SELECT profile_id, '', '', cv_file_url, 0, 'application/pdf'
FROM candidate_details
WHERE cv_file_url IS NOT NULL AND cv_file_url != '';

ALTER TABLE resumes ADD FOREIGN KEY (candidate_detail_id) REFERENCES candidate_details (profile_id) ON DELETE CASCADE;

ALTER TABLE candidate_details DROP COLUMN cv_file_url;

COMMENT ON TABLE resumes IS 'CV PDF đã upload của ứng viên (1-1 với candidate_details)';
