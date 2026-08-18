CREATE TABLE mail_messages (
    id UUID PRIMARY KEY,
    sender_player_id UUID REFERENCES players(id),
    recipient_player_id UUID NOT NULL REFERENCES players(id),
    message_type VARCHAR(32) NOT NULL DEFAULT 'PLAYER',
    subject VARCHAR(80) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP NULL,
    sender_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    source_type VARCHAR(64),
    source_id UUID,
    action_type VARCHAR(64),
    action_payload TEXT,
    delivery_key VARCHAR(128),
    CONSTRAINT chk_mail_subject_not_blank CHECK (length(btrim(subject)) > 0),
    CONSTRAINT chk_mail_body_not_blank CHECK (length(btrim(body)) > 0),
    CONSTRAINT chk_mail_message_type CHECK (message_type IN ('PLAYER', 'SYSTEM', 'AUCTION', 'CLAN', 'EVENT', 'ADMIN')),
    CONSTRAINT chk_mail_player_sender CHECK (message_type <> 'PLAYER' OR sender_player_id IS NOT NULL),
    CONSTRAINT uk_mail_delivery_key UNIQUE (delivery_key)
);

CREATE INDEX idx_mail_messages_recipient_inbox
    ON mail_messages(recipient_player_id, recipient_deleted, created_at DESC);
CREATE INDEX idx_mail_messages_sender_sent
    ON mail_messages(sender_player_id, sender_deleted, created_at DESC);
CREATE INDEX idx_mail_messages_unread
    ON mail_messages(recipient_player_id, recipient_deleted, read_at);
CREATE INDEX idx_mail_messages_rate_limit
    ON mail_messages(sender_player_id, created_at DESC);
