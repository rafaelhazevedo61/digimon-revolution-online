CREATE TABLE incubations (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    digitama_type VARCHAR(50) NOT NULL,
    incubator_type VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finish_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);
