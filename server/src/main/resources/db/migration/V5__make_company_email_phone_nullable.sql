-- TODO: email + phone sẽ được cập nhật qua form trong Employer Dashboard
ALTER TABLE companies ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE companies ALTER COLUMN email DROP NOT NULL;
