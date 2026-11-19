CREATE TABLE player_mission_progress (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    mission_id VARCHAR(100) NOT NULL,
    completion_count INT NOT NULL,
    CONSTRAINT uk_player_mission UNIQUE (player_id, mission_id)
);
