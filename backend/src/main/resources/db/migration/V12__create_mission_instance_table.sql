CREATE TABLE mission_instances (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    digimon_id UUID NOT NULL,
    mission_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    claimed_at TIMESTAMP
);