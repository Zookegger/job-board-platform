-- Fix profiles PK and FK direction to match DBML design
--
-- DBML (correct):  profiles.user_id is PK, detail tables reference profiles.user_id
-- V1 (buggy):      profiles.id is separate PK, FKs go backwards profiles -> details
--
-- JPA uses @MapsId, so Profile.id maps to column "user_id". The V1 profiles.id
-- column is completely ignored by JPA, and the backwards FKs block any valid INSERT.

DO $$
DECLARE
  conname text;
BEGIN
  -- Drop FK: profiles.id -> candidate_details.profile_id (backwards)
  SELECT c.conname INTO conname
  FROM pg_constraint c
  JOIN pg_class t ON t.oid = c.conrelid
  WHERE t.relname = 'profiles'
    AND c.contype = 'f'
    AND c.confrelid = (SELECT oid FROM pg_class WHERE relname = 'candidate_details');
  IF conname IS NOT NULL THEN
    EXECUTE 'ALTER TABLE profiles DROP CONSTRAINT ' || quote_ident(conname);
  END IF;

  -- Drop FK: profiles.id -> company_employer_details.profile_id (backwards)
  SELECT c.conname INTO conname
  FROM pg_constraint c
  JOIN pg_class t ON t.oid = c.conrelid
  WHERE t.relname = 'profiles'
    AND c.contype = 'f'
    AND c.confrelid = (SELECT oid FROM pg_class WHERE relname = 'company_employer_details');
  IF conname IS NOT NULL THEN
    EXECUTE 'ALTER TABLE profiles DROP CONSTRAINT ' || quote_ident(conname);
  END IF;

  -- Drop FK: applications.candidate_id -> profiles.id (wrong column referenced)
  SELECT c.conname INTO conname
  FROM pg_constraint c
  JOIN pg_class t ON t.oid = c.conrelid
  WHERE t.relname = 'applications'
    AND c.contype = 'f'
    AND c.confrelid = (SELECT oid FROM pg_class WHERE relname = 'profiles');
  IF conname IS NOT NULL THEN
    EXECUTE 'ALTER TABLE applications DROP CONSTRAINT ' || quote_ident(conname);
  END IF;
END;
$$;

-- Restructure profiles: remove separate id column, make user_id the PK
ALTER TABLE profiles DROP CONSTRAINT profiles_pkey;
ALTER TABLE profiles DROP CONSTRAINT profiles_user_id_key;
ALTER TABLE profiles DROP COLUMN id;
ALTER TABLE profiles ADD PRIMARY KEY (user_id);

-- Add correct-direction FKs matching DBML design
ALTER TABLE candidate_details
  ADD FOREIGN KEY (profile_id) REFERENCES profiles (user_id) ON DELETE CASCADE;

ALTER TABLE company_employer_details
  ADD FOREIGN KEY (profile_id) REFERENCES profiles (user_id) ON DELETE CASCADE;

ALTER TABLE applications
  ADD FOREIGN KEY (candidate_id) REFERENCES profiles (user_id) ON DELETE CASCADE;
