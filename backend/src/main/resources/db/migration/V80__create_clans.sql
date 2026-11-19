CREATE TABLE clans (
    id UUID PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    tag VARCHAR(5) NOT NULL UNIQUE,
    description VARCHAR(280),
    leader_id UUID NOT NULL,
    emblem VARCHAR(50),
    max_members INT NOT NULL DEFAULT 5,
    level INT NOT NULL DEFAULT 1,
    experience INT NOT NULL DEFAULT 0,
    bought_slots INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE players
    ADD COLUMN clan_id UUID REFERENCES clans(id),
    ADD COLUMN clan_role VARCHAR(20),
    ADD COLUMN clan_joined_at TIMESTAMP;

CREATE INDEX idx_players_clan_id ON players(clan_id);
