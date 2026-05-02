
CREATE TABLE digitama_history (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    digitama_type VARCHAR(20) NOT NULL,
    digimon_name VARCHAR(50) NOT NULL,
    digimon_id UUID NOT NULL,
    hatched_at TIMESTAMP NOT NULL,
    source VARCHAR(20) NOT NULL
);