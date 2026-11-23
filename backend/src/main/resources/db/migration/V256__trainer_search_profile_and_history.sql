-- Primeira observação de cada dia/regra: base para comparações futuras, sem histórico inventado.
CREATE TABLE trainer_ranking_daily_history (
    snapshot_date DATE NOT NULL,
    rules_version VARCHAR(32) NOT NULL,
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    observed_at TIMESTAMPTZ NOT NULL,
    position BIGINT NOT NULL,
    player_name VARCHAR(50) NOT NULL,
    collection_entries BIGINT NOT NULL,
    mastered_species BIGINT NOT NULL,
    completed_achievements BIGINT NOT NULL,
    highest_floor BIGINT NOT NULL,
    collection_score NUMERIC(30,0) NOT NULL,
    achievement_score NUMERIC(30,0) NOT NULL,
    tower_score NUMERIC(30,0) NOT NULL,
    total_score NUMERIC(30,0) NOT NULL,
    PRIMARY KEY (snapshot_date, rules_version, player_id)
);
CREATE INDEX idx_trainer_history_player_date ON trainer_ranking_daily_history(player_id, snapshot_date DESC);
