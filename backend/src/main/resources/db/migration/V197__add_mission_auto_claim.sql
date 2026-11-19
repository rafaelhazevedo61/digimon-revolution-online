ALTER TABLE mission_instances
    ADD COLUMN auto_claim_enabled BOOLEAN NOT NULL DEFAULT FALSE;
