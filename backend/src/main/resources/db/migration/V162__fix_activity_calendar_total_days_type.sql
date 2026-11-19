ALTER TABLE activity_calendar_monthly
    ALTER COLUMN total_days TYPE INTEGER
    USING total_days::INTEGER;
