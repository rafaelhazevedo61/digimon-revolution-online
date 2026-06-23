-- Boss attempts table
CREATE TABLE boss_attempts (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    digimon_id UUID NOT NULL,
    boss_id BIGINT NOT NULL REFERENCES boss_definitions(id),
    status VARCHAR(20) NOT NULL,  -- VICTORY, DEFEAT
    damage_dealt INT NOT NULL DEFAULT 0,
    xp_gained INT NOT NULL DEFAULT 0,
    bits_gained INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_boss_attempts_player_id ON boss_attempts(player_id);
CREATE INDEX idx_boss_attempts_boss_id ON boss_attempts(boss_id);
CREATE INDEX idx_boss_attempts_player_boss ON boss_attempts(player_id, boss_id, created_at DESC);
