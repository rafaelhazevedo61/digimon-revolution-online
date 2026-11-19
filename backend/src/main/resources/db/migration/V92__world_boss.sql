-- Boss Mundial: instância global compartilhada por todo o servidor e ataques dos jogadores
CREATE TABLE world_boss_instances (
    id UUID PRIMARY KEY,
    boss_id BIGINT NOT NULL REFERENCES boss_definitions(id),
    max_hp INT NOT NULL,
    remaining_hp INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DEFEATED, EXPIRED
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    defeated_at TIMESTAMP,
    daily_reset_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_world_boss_instances_created_at ON world_boss_instances(created_at DESC);
CREATE INDEX idx_world_boss_instances_status ON world_boss_instances(status);

CREATE TABLE world_boss_attacks (
    id UUID PRIMARY KEY,
    world_boss_id UUID NOT NULL REFERENCES world_boss_instances(id),
    player_id UUID NOT NULL REFERENCES players(id),
    digimon_id UUID NOT NULL REFERENCES digimons(id),
    damage INT NOT NULL DEFAULT 0,
    energy_cost INT NOT NULL DEFAULT 0,
    bits_gained INT NOT NULL DEFAULT 0,
    xp_gained INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_world_boss_attacks_boss_id ON world_boss_attacks(world_boss_id);
CREATE INDEX idx_world_boss_attacks_player_id ON world_boss_attacks(player_id);
CREATE INDEX idx_world_boss_attacks_boss_created ON world_boss_attacks(world_boss_id, created_at DESC);

INSERT INTO boss_definitions (
    code, name, boss_type, required_stage, required_level, required_rebirths,
    hp, atk, def, energy_cost, cooldown_minutes, base_xp_reward, base_bits_reward, defeat_xp_percent,
    image_url, active
) VALUES (
    'WORLD_BOSS_APOCALYMON',
    'Apocalymon (Boss Mundial)',
    'WORLD',
    'BABY',
    1,
    0,
    1000000,
    5000,
    4000,
    20,
    0,
    5000,
    200,
    5,
    '/assets/img/world-boss-apocalymon.png',
    TRUE
);
