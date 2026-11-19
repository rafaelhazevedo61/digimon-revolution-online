ALTER TABLE event_rewards
    ADD COLUMN IF NOT EXISTS item_definition_code VARCHAR(128);

COMMENT ON COLUMN event_rewards.item_definition_code IS
    'Código específico de item_definitions usado na entrega; nulo em premiações legadas por itemType.';
