ALTER TABLE activity_calendar_monthly
    ALTER COLUMN claimed_days TYPE INTEGER
    USING claimed_days::INTEGER;
