ALTER TABLE players
    ADD COLUMN incubation_auto_repeat_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN incubation_auto_claim_enabled BOOLEAN NOT NULL DEFAULT FALSE;
