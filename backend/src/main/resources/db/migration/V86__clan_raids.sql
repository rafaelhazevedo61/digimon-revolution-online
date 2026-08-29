-- Raid de clã: instância compartilhada e ataques dos membros
CREATE TABLE clan_raid_instances (
    id UUID PRIMARY KEY,
    clan_id UUID NOT NULL REFERENCES clans(id),
    boss_id BIGINT NOT NULL REFERENCES boss_definitions(id),
    max_hp INT NOT NULL,
    remaining_hp INT NOT NULL,
    status VARCHAR(20) NOT NULL, -- ACTIVE, DEFEATED, EXPIRED
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    defeated_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_clan_raid_instances_clan_id ON clan_raid_instances(clan_id);
CREATE INDEX idx_clan_raid_instances_status ON clan_raid_instances(status);
CREATE INDEX idx_clan_raid_instances_clan_status ON clan_raid_instances(clan_id, status);

CREATE TABLE clan_raid_attacks (
    id UUID PRIMARY KEY,
    clan_raid_id UUID NOT NULL REFERENCES clan_raid_instances(id),
    player_id UUID NOT NULL REFERENCES players(id),
    digimon_id UUID NOT NULL REFERENCES digimons(id),
    damage INT NOT NULL DEFAULT 0,
    energy_cost INT NOT NULL DEFAULT 0,
    bits_gained INT NOT NULL DEFAULT 0,
    xp_gained INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clan_raid_attacks_raid_id ON clan_raid_attacks(clan_raid_id);
CREATE INDEX idx_clan_raid_attacks_player_id ON clan_raid_attacks(player_id);
CREATE INDEX idx_clan_raid_attacks_raid_created ON clan_raid_attacks(clan_raid_id, created_at DESC);

-- Boss de raid de clã (HP alto, exige cooperação)
INSERT INTO boss_definitions (
    code, name, boss_type, required_stage, required_level, required_rebirths,
    hp, atk, def, energy_cost, cooldown_minutes, base_xp_reward, base_bits_reward, defeat_xp_percent,
    image_url, active
) VALUES (
    'CLAN_RAID_OMEGAMON',
    'Omegamon (Raid de Clã)',
    'CLAN',
    'MEGA',
    60,
    0,
    50000,
    900,
    700,
    15,
    30,
    2500,
    1000,
    5,
    'https://digimon.shadowsmith.com/img/omegamon.jpg',
    TRUE
);
