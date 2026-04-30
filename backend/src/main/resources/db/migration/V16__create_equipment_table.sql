CREATE TABLE equipments (
    id UUID PRIMARY KEY,
    digimon_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    slot VARCHAR(30) NOT NULL,
    rarity VARCHAR(30) NOT NULL,
    bonus_hp INT NOT NULL DEFAULT 0,
    bonus_attack INT NOT NULL DEFAULT 0,
    bonus_defense INT NOT NULL DEFAULT 0,
    equipped BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_equipments_digimon_id ON equipments(digimon_id);
