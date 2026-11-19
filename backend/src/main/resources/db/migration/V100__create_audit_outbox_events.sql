CREATE TABLE audit_outbox_events (
    id UUID PRIMARY KEY,
    event_id VARCHAR(120) NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(80) NOT NULL,
    correlation_id VARCHAR(64),
    payload_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    available_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    last_error VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_audit_outbox_available
    ON audit_outbox_events (status, available_at, created_at);

CREATE INDEX idx_audit_outbox_correlation
    ON audit_outbox_events (correlation_id);
