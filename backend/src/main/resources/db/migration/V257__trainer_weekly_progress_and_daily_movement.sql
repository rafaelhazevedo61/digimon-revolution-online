CREATE FUNCTION trainer_ranking_rules_version() RETURNS TEXT LANGUAGE SQL STABLE AS $$
    SELECT MD5('trainer-v1;collection=10;mastery=50;tower=20;' || COALESCE(
        STRING_AGG(code || ':' || trainer_points::text, ';' ORDER BY code), ''))
    FROM achievement_definitions WHERE active = TRUE
$$;

ALTER TABLE trainer_ranking_state
    ADD COLUMN rules_version VARCHAR(32),
    ADD COLUMN week_start DATE,
    ADD COLUMN week_baseline_at TIMESTAMPTZ,
    ADD COLUMN comparison_date DATE,
    ADD COLUMN comparison_available BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE trainer_ranking_entries
    ADD COLUMN weekly_gain NUMERIC(30,0) NOT NULL DEFAULT 0,
    ADD COLUMN weekly_position BIGINT,
    ADD COLUMN weekly_baseline_at TIMESTAMPTZ,
    ADD COLUMN weekly_baseline_kind VARCHAR(20),
    ADD COLUMN movement VARCHAR(20) NOT NULL DEFAULT 'UNAVAILABLE',
    ADD COLUMN position_change BIGINT;
CREATE INDEX idx_trainer_weekly_position ON trainer_ranking_entries(weekly_position);

-- Marker distinguishes a missing day from a recorded day with no participants.
CREATE TABLE trainer_ranking_recorded_days (
    snapshot_date DATE NOT NULL,
    rules_version VARCHAR(32) NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (snapshot_date, rules_version)
);
INSERT INTO trainer_ranking_recorded_days
SELECT snapshot_date, rules_version, MIN(observed_at)
FROM trainer_ranking_daily_history GROUP BY snapshot_date, rules_version;

CREATE TABLE trainer_ranking_weeks (
    week_start DATE NOT NULL,
    rules_version VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (week_start, rules_version)
);
CREATE TABLE trainer_ranking_week_baselines (
    week_start DATE NOT NULL,
    rules_version VARCHAR(32) NOT NULL,
    player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    baseline_score NUMERIC(30,0),
    baseline_at TIMESTAMPTZ NOT NULL,
    baseline_kind VARCHAR(20) NOT NULL,
    PRIMARY KEY (week_start, rules_version, player_id),
    FOREIGN KEY (week_start, rules_version) REFERENCES trainer_ranking_weeks(week_start, rules_version) ON DELETE CASCADE
);
