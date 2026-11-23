-- Pontuação independente da recompensa e do resgate das conquistas.
ALTER TABLE achievement_definitions ADD COLUMN trainer_points INTEGER NOT NULL DEFAULT 25
    CHECK (trainer_points IN (25, 50, 100));
UPDATE achievement_definitions SET trainer_points = 50 WHERE code IN ('HATCH_25', 'HATCH_50', 'STAGE_CHAMPION', 'STAGE_ULTIMATE', 'REBIRTH_5', 'REBIRTH_10', 'SPECIES_25', 'SPECIES_50', 'SPECIES_75', 'MISSION_100', 'MISSION_250', 'ARENA_25', 'ARENA_50', 'ARENA_100', 'WORLD_50', 'WORLD_100', 'RAID_25', 'RAID_50', 'REFINE_5', 'ASCEND_1', 'BIRTH_EPIC', 'BIRTH_PERFECT_IV', 'BIRTH_TRAIT_10', 'MASTERY_1', 'LINES_1', 'COLLECTION_ROOKIE_10', 'COLLECTION_CHAMPION_10', 'EXPLORE_GEAR_SAVANNA', 'EXPLORE_FACTORIAL_TOWN', 'TEAM_100', 'SET_1', 'ASCENSION_DONE_1', 'BOSS_WIN_10', 'BOSS_DISTINCT_5', 'WORLD_PARTICIPATION', 'RAID_PARTICIPATION');
UPDATE achievement_definitions SET trainer_points = 100 WHERE code IN ('HATCH_100', 'HATCH_250', 'HATCH_500', 'STAGE_MEGA', 'REBIRTH_25', 'REBIRTH_50', 'REBIRTH_100', 'SPECIES_100', 'SPECIES_150', 'MISSION_500', 'MISSION_1000', 'MISSION_2500', 'MISSION_5000', 'ARENA_250', 'ARENA_500', 'WORLD_200', 'WORLD_500', 'RAID_100', 'RAID_250', 'REFINE_10', 'BIRTH_LEGENDARY', 'BIRTH_ELITE_10', 'BIRTH_ALL_PERFECT_IV', 'MASTERY_5', 'MASTERY_10', 'LINES_5', 'LINES_10', 'COLLECTION_ULTIMATE_10', 'COLLECTION_MEGA_10', 'EXPLORE_FREEZELAND', 'EXPLORE_SERVER_DESERT', 'EXPLORE_INFINITY_MOUNTAIN', 'TEAM_500', 'SET_3', 'REFINE_SUCCESS_50', 'ASCENSION_DONE_10', 'BOSS_WIN_100');

-- Exclusão explícita de contas de teste, sem inferir pelo nome/e-mail.
ALTER TABLE players ADD COLUMN trainer_ranking_excluded BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_trainer_ranking_bot_players ON digimons(player_id) WHERE is_bot = TRUE;

CREATE TABLE trainer_ranking_state (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    updated_at TIMESTAMPTZ,
    total_players BIGINT NOT NULL DEFAULT 0
);
INSERT INTO trainer_ranking_state(id) VALUES (1);

CREATE TABLE trainer_ranking_entries (
    player_id UUID PRIMARY KEY REFERENCES players(id) ON DELETE CASCADE,
    position BIGINT NOT NULL UNIQUE CHECK (position > 0),
    player_name VARCHAR(50) NOT NULL,
    collection_entries BIGINT NOT NULL,
    mastered_species BIGINT NOT NULL,
    completed_achievements BIGINT NOT NULL,
    highest_floor BIGINT NOT NULL,
    collection_score NUMERIC(30,0) NOT NULL,
    achievement_score NUMERIC(30,0) NOT NULL,
    tower_score NUMERIC(30,0) NOT NULL,
    total_score NUMERIC(30,0) NOT NULL
);
