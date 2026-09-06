ALTER TABLE mission_instances
    ADD COLUMN automation_pause_reason VARCHAR(64),
    ADD COLUMN automation_paused_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN automation_last_error_code VARCHAR(64);

ALTER TABLE incubations
    ADD COLUMN automation_pause_reason VARCHAR(64),
    ADD COLUMN automation_paused_at TIMESTAMP,
    ADD COLUMN automation_last_error_code VARCHAR(64);
