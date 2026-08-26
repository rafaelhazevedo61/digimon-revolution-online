CREATE TABLE event_reward_items (
    id UUID PRIMARY KEY,
    event_reward_id UUID NOT NULL REFERENCES event_rewards(id) ON DELETE CASCADE,
    item_type VARCHAR(50) NOT NULL,
    item_definition_code VARCHAR(128),
    item_quantity INTEGER NOT NULL,
    position INTEGER NOT NULL,
    CONSTRAINT chk_event_reward_item_quantity CHECK (item_quantity > 0),
    CONSTRAINT chk_event_reward_item_position CHECK (position >= 0),
    CONSTRAINT uk_event_reward_item_position UNIQUE (event_reward_id, position),
    CONSTRAINT uk_event_reward_item_definition UNIQUE (event_reward_id, item_definition_code)
);

CREATE INDEX idx_event_reward_items_reward
    ON event_reward_items(event_reward_id, position);
