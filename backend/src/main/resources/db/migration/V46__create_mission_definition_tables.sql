-- Mission definitions (replaces hardcoded MissionCatalog.java)
CREATE TABLE mission_definitions (
    id          VARCHAR(80) PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    area        VARCHAR(40)  NOT NULL,
    required_stage VARCHAR(20) NOT NULL,
    required_level INT         NOT NULL,
    base_xp     INT          NOT NULL,
    energy_cost INT          NOT NULL,
    duration_seconds INT      NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Fixed rewards per mission
CREATE TABLE mission_rewards (
    id              BIGSERIAL PRIMARY KEY,
    mission_id      VARCHAR(80) NOT NULL REFERENCES mission_definitions(id),
    item_type       VARCHAR(40) NOT NULL,
    base_quantity   INT         NOT NULL
);

-- Loot rarity chances per mission
CREATE TABLE mission_loot_chances (
    id              BIGSERIAL PRIMARY KEY,
    mission_id      VARCHAR(80) NOT NULL REFERENCES mission_definitions(id),
    rarity          VARCHAR(20) NOT NULL,
    chance          INT         NOT NULL
);

-- Loot items per mission
CREATE TABLE mission_loot_items (
    id              BIGSERIAL PRIMARY KEY,
    mission_id      VARCHAR(80) NOT NULL REFERENCES mission_definitions(id),
    rarity          VARCHAR(20) NOT NULL,
    item_type       VARCHAR(40) NOT NULL,
    quantity        INT         NOT NULL
);
