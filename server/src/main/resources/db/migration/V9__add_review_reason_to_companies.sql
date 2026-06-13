CREATE TYPE review_reason AS ENUM ('NEW_COMPANY', 'INFO_UPDATED');
ALTER TABLE companies ADD COLUMN review_reason review_reason;
