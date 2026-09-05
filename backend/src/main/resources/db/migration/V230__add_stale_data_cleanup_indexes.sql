CREATE INDEX IF NOT EXISTS idx_mission_instances_claimed_cleanup
    ON mission_instances (claimed_at, id)
    WHERE status = 'CLAIMED' AND claimed_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_audit_outbox_cleanup
    ON audit_outbox_events (status, published_at, created_at, id)
    WHERE status IN ('PUBLISHED', 'DEAD_LETTER');
