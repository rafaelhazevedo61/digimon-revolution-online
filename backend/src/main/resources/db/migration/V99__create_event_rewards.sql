CREATE TABLE event_rewards (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL REFERENCES players(id),
    source_type VARCHAR(64) NOT NULL,
    source_id VARCHAR(128) NOT NULL,
    subject VARCHAR(80) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    bits_amount INTEGER NOT NULL DEFAULT 0,
    item_type VARCHAR(50),
    item_quantity INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    claimed_at TIMESTAMP NULL,
    CONSTRAINT chk_event_reward_status
        CHECK (status IN ('PENDING', 'CLAIMED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT chk_event_reward_amounts
        CHECK (bits_amount >= 0 AND item_quantity >= 0 AND (bits_amount > 0 OR item_quantity > 0)),
    CONSTRAINT chk_event_reward_item
        CHECK ((item_type IS NULL AND item_quantity = 0) OR (item_type IS NOT NULL AND item_quantity > 0)),
    CONSTRAINT chk_event_reward_dates
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_event_reward_player_status
    ON event_rewards(player_id, status, expires_at);
CREATE INDEX idx_event_reward_source
    ON event_rewards(source_type, source_id);
CREATE UNIQUE INDEX uk_event_reward_source_player
    ON event_rewards(source_type, source_id, player_id);
