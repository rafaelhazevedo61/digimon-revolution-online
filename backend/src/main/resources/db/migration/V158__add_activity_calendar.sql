CREATE TABLE activity_calendar_daily (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    activity_date DATE NOT NULL,
    year_month VARCHAR(7) NOT NULL,
    points INTEGER NOT NULL DEFAULT 0 CHECK (points >= 0),
    goal_reached_at TIMESTAMPTZ,
    reward_claimed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_activity_calendar_daily_player_date UNIQUE (player_id, activity_date),
    CONSTRAINT ck_activity_calendar_daily_claimed_goal CHECK (reward_claimed_at IS NULL OR goal_reached_at IS NOT NULL)
);

CREATE INDEX idx_activity_calendar_daily_player_date ON activity_calendar_daily (player_id, activity_date);

CREATE TABLE activity_calendar_monthly (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    year_month VARCHAR(7) NOT NULL,
    total_days SMALLINT NOT NULL CHECK (total_days BETWEEN 28 AND 31),
    claimed_days SMALLINT NOT NULL DEFAULT 0 CHECK (claimed_days BETWEEN 0 AND total_days),
    monthly_completion_eligible_at TIMESTAMPTZ,
    monthly_reward_claimed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_activity_calendar_monthly_player_month UNIQUE (player_id, year_month),
    CONSTRAINT ck_activity_calendar_monthly_claimed_eligible CHECK (monthly_reward_claimed_at IS NULL OR monthly_completion_eligible_at IS NOT NULL)
);

CREATE TABLE activity_point_events (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    activity_date DATE NOT NULL,
    source VARCHAR(40) NOT NULL,
    source_reference_id VARCHAR(120) NOT NULL,
    points INTEGER NOT NULL CHECK (points > 0),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_activity_point_event_source UNIQUE (player_id, source, source_reference_id)
);

CREATE INDEX idx_activity_point_events_player_date ON activity_point_events (player_id, activity_date);
