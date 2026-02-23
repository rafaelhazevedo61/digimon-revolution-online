CREATE TABLE digimons (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    stage VARCHAR(50) NOT NULL,
    level INT NOT NULL,
    experience INT NOT NULL,
    hp INT,
    attack INT,
    defense INT,
    iv_hp INT,
    iv_attack INT,
    iv_defense INT,
    created_at TIMESTAMP NOT NULL
);
