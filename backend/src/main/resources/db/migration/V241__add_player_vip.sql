ALTER TABLE players ADD COLUMN vip_expires_at TIMESTAMP WITH TIME ZONE;

UPDATE players
SET incubation_auto_claim_enabled = FALSE, incubation_auto_repeat_enabled = FALSE
WHERE incubation_auto_claim_enabled OR incubation_auto_repeat_enabled;

UPDATE mission_instances
SET auto_claim_enabled = FALSE, auto_repeat_enabled = FALSE
WHERE status IN ('RUNNING', 'COMPLETED') AND (auto_claim_enabled OR auto_repeat_enabled);

UPDATE incubations
SET auto_claim_enabled = FALSE, auto_repeat_enabled = FALSE
WHERE status <> 'CLAIMED' AND (auto_claim_enabled OR auto_repeat_enabled);
