BEGIN;

-- Loot tables are named, reusable catalogs of rarity weights and item entries.
CREATE TABLE loot_tables (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(80)  NOT NULL UNIQUE,
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(80)  NOT NULL DEFAULT 'SYSTEM',
    updated_by  VARCHAR(80)  NOT NULL DEFAULT 'SYSTEM'
);

CREATE TABLE loot_table_rarity_weights (
    id           BIGSERIAL PRIMARY KEY,
    loot_table_id BIGINT      NOT NULL REFERENCES loot_tables(id) ON DELETE CASCADE,
    rarity       VARCHAR(20)  NOT NULL,
    weight       INT          NOT NULL,
    CONSTRAINT uq_loot_table_rarity UNIQUE (loot_table_id, rarity),
    CONSTRAINT ck_loot_table_rarity_weight_positive CHECK (weight > 0),
    CONSTRAINT ck_loot_table_rarity_known CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'))
);

CREATE TABLE loot_table_entries (
    id            BIGSERIAL PRIMARY KEY,
    loot_table_id BIGINT      NOT NULL REFERENCES loot_tables(id) ON DELETE CASCADE,
    rarity        VARCHAR(20)  NOT NULL,
    item_type     VARCHAR(50)  NOT NULL,
    material_code VARCHAR(80),
    weight        INT          NOT NULL,
    min_quantity  INT          NOT NULL,
    max_quantity  INT          NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_loot_entry_rarity_known CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY')),
    CONSTRAINT ck_loot_entry_weight_positive CHECK (weight > 0),
    CONSTRAINT ck_loot_entry_quantity_range CHECK (min_quantity > 0 AND max_quantity >= min_quantity),
    CONSTRAINT ck_loot_entry_material_code CHECK (
        (item_type IN ('EVOLUTION_MATERIAL', 'LOOT_CHEST') AND material_code IS NOT NULL AND btrim(material_code) <> '')
        OR (item_type NOT IN ('EVOLUTION_MATERIAL', 'LOOT_CHEST'))
    )
);

CREATE INDEX idx_loot_table_entries_lookup
    ON loot_table_entries (loot_table_id, rarity, active);

CREATE TABLE chest_definitions (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(120) NOT NULL,
    description       TEXT,
    icon              VARCHAR(120),
    loot_table_id     BIGINT      NOT NULL REFERENCES loot_tables(id),
    item_definition_id BIGINT      NOT NULL UNIQUE REFERENCES item_definitions(id),
    tradable          BOOLEAN     NOT NULL DEFAULT TRUE,
    active            BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(80) NOT NULL DEFAULT 'SYSTEM',
    updated_by        VARCHAR(80) NOT NULL DEFAULT 'SYSTEM'
);

CREATE INDEX idx_chest_definitions_active ON chest_definitions (active);

-- The opening record is created in Sprint 2. The request key already exists here
-- so retries can be made idempotent without changing the schema later.
CREATE TABLE chest_openings (
    id                 BIGSERIAL PRIMARY KEY,
    request_id         VARCHAR(120) NOT NULL UNIQUE,
    player_id          UUID         NOT NULL,
    chest_definition_id BIGINT      NOT NULL REFERENCES chest_definitions(id),
    rarity             VARCHAR(20)  NOT NULL,
    source             VARCHAR(120) NOT NULL,
    opened_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_chest_opening_rarity_known CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'))
);

CREATE TABLE chest_opening_items (
    id            BIGSERIAL PRIMARY KEY,
    chest_opening_id BIGINT      NOT NULL REFERENCES chest_openings(id) ON DELETE CASCADE,
    item_type     VARCHAR(50)  NOT NULL,
    material_code VARCHAR(80),
    quantity      INT          NOT NULL,
    CONSTRAINT ck_chest_opening_item_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_chest_opening_item_material_code CHECK (
        (item_type IN ('EVOLUTION_MATERIAL', 'LOOT_CHEST') AND material_code IS NOT NULL AND btrim(material_code) <> '')
        OR (item_type NOT IN ('EVOLUTION_MATERIAL', 'LOOT_CHEST'))
    )
);

CREATE INDEX idx_chest_openings_player ON chest_openings (player_id, opened_at);

-- A single reusable baseline is intentionally seeded. Sprint 3 will associate
-- missions and can clone or specialize this table through the admin model.
INSERT INTO loot_tables (code, name, description, active)
VALUES (
    'LOOT_TABLE_MISSION_AREA_DEFAULT',
    'Tabela padrão de Baú da Área',
    'Tabela inicial reutilizável para Missões; os pesos e entradas serão refinados antes da ativação da entrega em jogo.',
    TRUE
);

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT id, data.rarity, data.weight
FROM loot_tables
CROSS JOIN (VALUES
    ('COMMON', 70),
    ('RARE', 20),
    ('EPIC', 8),
    ('LEGENDARY', 2)
) AS data(rarity, weight)
WHERE code = 'LOOT_TABLE_MISSION_AREA_DEFAULT';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight, min_quantity, max_quantity, active
)
SELECT lt.id, data.rarity, data.item_type, data.material_code, data.weight,
       data.min_quantity, data.max_quantity, TRUE
FROM loot_tables lt
CROSS JOIN (VALUES
    ('COMMON',    'TRAINING_STONE',     NULL,                    50, 1, 3),
    ('COMMON',    'DATA_CORE',          NULL,                    30, 1, 2),
    ('COMMON',    'EVOLUTION_MATERIAL', 'FRAGMENT_KOROMON',       20, 1, 5),
    ('RARE',      'DIGITAMA_NATURE',    NULL,                    45, 1, 1),
    ('RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_AGUMON',        35, 1, 5),
    ('RARE',      'INCUBATOR_COMMON',   NULL,                    20, 1, 1),
    ('EPIC',      'INCUBATOR_RARE',     NULL,                    45, 1, 1),
    ('EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_GREYMON',       35, 1, 3),
    ('EPIC',      'DATA_CORE',          NULL,                    20, 2, 5),
    ('LEGENDARY', 'INCUBATOR_EPIC',     NULL,                    45, 1, 1),
    ('LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_METALGREYMON',  35, 1, 2),
    ('LEGENDARY', 'DATA_CORE',          NULL,                    20, 3, 8)
) AS data(rarity, item_type, material_code, weight, min_quantity, max_quantity)
WHERE lt.code = 'LOOT_TABLE_MISSION_AREA_DEFAULT';

INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
    ('CHEST_MISSION_NATIVE_FOREST', 'Baú Floresta Nativa', 'Baú obtido nas Missões da Floresta Nativa.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_mission_native_forest'),
    ('CHEST_MISSION_GEAR_SAVANNA', 'Baú Gear Savanna', 'Baú obtido nas Missões da Gear Savanna.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_mission_gear_savanna'),
    ('CHEST_MISSION_FACTORIAL_TOWN', 'Baú Factorial Town', 'Baú obtido nas Missões da Factorial Town.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_mission_factorial_town'),
    ('CHEST_MISSION_FREEZELAND', 'Baú Freezeland', 'Baú obtido nas Missões de Freezeland.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_mission_freezeland'),
    ('CHEST_MISSION_SERVER_DESERT', 'Baú Server Desert', 'Baú obtido nas Missões do Server Desert.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_mission_server_desert'),
    ('CHEST_MISSION_INFINITY_MOUNTAIN', 'Baú Infinity Mountain', 'Baú obtido nas Missões da Infinity Mountain.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_mission_infinity_mountain')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

INSERT INTO chest_definitions (
    code, name, description, icon, loot_table_id, item_definition_id, tradable, active
)
SELECT data.code, data.name, data.description, data.icon, lt.id, item.id, TRUE, TRUE
FROM loot_tables lt
JOIN (VALUES
    ('CHEST_MISSION_NATIVE_FOREST', 'Baú Floresta Nativa', 'Baú obtido nas Missões da Floresta Nativa.', 'chest_mission_native_forest'),
    ('CHEST_MISSION_GEAR_SAVANNA', 'Baú Gear Savanna', 'Baú obtido nas Missões da Gear Savanna.', 'chest_mission_gear_savanna'),
    ('CHEST_MISSION_FACTORIAL_TOWN', 'Baú Factorial Town', 'Baú obtido nas Missões da Factorial Town.', 'chest_mission_factorial_town'),
    ('CHEST_MISSION_FREEZELAND', 'Baú Freezeland', 'Baú obtido nas Missões de Freezeland.', 'chest_mission_freezeland'),
    ('CHEST_MISSION_SERVER_DESERT', 'Baú Server Desert', 'Baú obtido nas Missões do Server Desert.', 'chest_mission_server_desert'),
    ('CHEST_MISSION_INFINITY_MOUNTAIN', 'Baú Infinity Mountain', 'Baú obtido nas Missões da Infinity Mountain.', 'chest_mission_infinity_mountain')
) AS data(code, name, description, icon) ON TRUE
JOIN item_definitions item ON item.code = data.code
WHERE lt.code = 'LOOT_TABLE_MISSION_AREA_DEFAULT'
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active;

COMMIT;
