CREATE TABLE player_arena_statistics (
    player_id UUID PRIMARY KEY REFERENCES players(id) ON DELETE CASCADE,
    arena_points_won BIGINT NOT NULL DEFAULT 0,
    arena_points_lost BIGINT NOT NULL DEFAULT 0,
    arena_wins BIGINT NOT NULL DEFAULT 0,
    arena_losses BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
