-- Snapshot do último combate; não depende do estado atual ou da existência do Digimon.
-- Resultados anteriores à migration permanecem sem detalhes, sem inventar HP retroativo.
CREATE TABLE player_tower_last_members (
    player_id UUID NOT NULL REFERENCES player_tower_progress(player_id) ON DELETE CASCADE,
    position SMALLINT NOT NULL CHECK (position BETWEEN 0 AND 2),
    digimon_id UUID NOT NULL,
    digimon_name VARCHAR(160) NOT NULL,
    remaining_hp INTEGER NOT NULL CHECK (remaining_hp >= 0),
    max_hp INTEGER NOT NULL CHECK (max_hp > 0 AND remaining_hp <= max_hp),
    PRIMARY KEY (player_id, position)
);
