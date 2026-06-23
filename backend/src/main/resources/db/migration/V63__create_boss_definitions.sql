-- Boss definitions table
CREATE TABLE boss_definitions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    boss_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',  -- NORMAL, DAILY, WEEKLY, MONTHLY
    required_stage VARCHAR(20) NOT NULL DEFAULT 'ROOKIE',
    required_level INT NOT NULL DEFAULT 1,
    required_rebirths INT NOT NULL DEFAULT 0,
    hp INT NOT NULL,
    atk INT NOT NULL,
    def INT NOT NULL,
    energy_cost INT NOT NULL DEFAULT 5,
    cooldown_minutes INT NOT NULL DEFAULT 360,  -- 6h for NORMAL
    base_xp_reward INT NOT NULL,
    base_bits_reward INT NOT NULL,
    defeat_xp_percent INT NOT NULL DEFAULT 10,  -- % of baseXpReward given on defeat
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Boss drops table
CREATE TABLE boss_drops (
    id BIGSERIAL PRIMARY KEY,
    boss_id BIGINT NOT NULL REFERENCES boss_definitions(id) ON DELETE CASCADE,
    drop_type VARCHAR(20) NOT NULL DEFAULT 'ITEM',  -- ITEM or EQUIPMENT
    item_code VARCHAR(100),           -- for ITEM drops
    template_name VARCHAR(200),       -- for EQUIPMENT drops
    equipment_rarity VARCHAR(20),     -- for EQUIPMENT drops
    chance INT NOT NULL,              -- percentage (1-100)
    min_quantity INT NOT NULL DEFAULT 1,
    max_quantity INT NOT NULL DEFAULT 1
);

CREATE INDEX idx_boss_drops_boss_id ON boss_drops(boss_id);
