-- Arena / PvP assíncrono.
-- Rating (estilo ELO) e histórico de vitórias/derrotas por Digimon.
ALTER TABLE digimons ADD COLUMN arena_rating INT NOT NULL DEFAULT 1000;
ALTER TABLE digimons ADD COLUMN arena_wins INT NOT NULL DEFAULT 0;
ALTER TABLE digimons ADD COLUMN arena_losses INT NOT NULL DEFAULT 0;

CREATE INDEX idx_digimons_arena_rating ON digimons (arena_rating DESC);

-- Histórico de partidas de arena.
CREATE TABLE arena_matches (
    id                    UUID PRIMARY KEY,
    attacker_player_id    UUID NOT NULL,
    attacker_digimon_id   UUID NOT NULL,
    defender_player_id    UUID NOT NULL,
    defender_digimon_id   UUID NOT NULL,
    attacker_won          BOOLEAN NOT NULL,
    attacker_power        INT NOT NULL,
    defender_power        INT NOT NULL,
    win_chance            INT NOT NULL,
    attacker_rating_change INT NOT NULL,
    attacker_rating_after  INT NOT NULL,
    defender_rating_change INT NOT NULL,
    defender_rating_after  INT NOT NULL,
    bits_gained           INT NOT NULL,
    created_at            TIMESTAMP NOT NULL
);

CREATE INDEX idx_arena_matches_attacker ON arena_matches (attacker_player_id, created_at DESC);
CREATE INDEX idx_arena_matches_defender ON arena_matches (defender_player_id, created_at DESC);
