-- Uma linha por conta: progresso permanente e último resultado para repetição segura.
CREATE TABLE player_tower_progress (
    player_id UUID PRIMARY KEY REFERENCES players(id) ON DELETE CASCADE,
    highest_floor BIGINT NOT NULL DEFAULT 0 CHECK (highest_floor >= 0),
    attempts BIGINT NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    available_at TIMESTAMPTZ,
    request_id UUID,
    last_floor BIGINT,
    last_won BOOLEAN,
    last_rounds INTEGER,
    last_bits INTEGER,
    last_item_code VARCHAR(80),
    last_item_name VARCHAR(160),
    last_quantity INTEGER,
    last_team_name VARCHAR(40),
    last_enemy_name VARCHAR(80)
);
