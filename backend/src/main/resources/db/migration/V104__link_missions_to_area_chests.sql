ALTER TABLE mission_definitions
    ADD COLUMN chest_definition_id BIGINT REFERENCES chest_definitions(id);

CREATE INDEX idx_mission_definitions_chest
    ON mission_definitions (chest_definition_id);

-- Cada missão recebe uma loot table nomeada. Missões que precisarem compartilhar
-- uma pool poderão apontar para a mesma definição em uma migration administrativa.
INSERT INTO loot_tables (code, name, description, active)
SELECT
    'LOOT_TABLE_MISSION_' || md.id,
    'Loot da missão — ' || md.name,
    'Pool inicial migrada da configuração legada da missão. Pode ser editada pelo painel administrativo.',
    TRUE
FROM mission_definitions md
ON CONFLICT (code) DO NOTHING;

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT
    lt.id,
    mlc.rarity,
    mlc.chance
FROM mission_loot_chances mlc
JOIN mission_definitions md ON md.id = mlc.mission_id
JOIN loot_tables lt ON lt.code = 'LOOT_TABLE_MISSION_' || md.id
ON CONFLICT (loot_table_id, rarity) DO UPDATE SET weight = EXCLUDED.weight;

-- Itens comuns, Digitamas e Incubadoras são migrados para as novas pools.
-- Os fragmentos genéricos por estágio permanecem legados e não entram nas novas pools.
INSERT INTO loot_table_entries (
    loot_table_id,
    rarity,
    item_type,
    material_code,
    weight,
    min_quantity,
    max_quantity,
    active
)
SELECT
    lt.id,
    mli.rarity,
    mli.item_type,
    NULL,
    1,
    mli.quantity,
    mli.quantity,
    TRUE
FROM mission_loot_items mli
JOIN mission_definitions md ON md.id = mli.mission_id
JOIN loot_tables lt ON lt.code = 'LOOT_TABLE_MISSION_' || md.id
WHERE mli.item_type NOT IN (
    'FRAGMENT_ROOKIE',
    'FRAGMENT_CHAMPION',
    'FRAGMENT_ULTIMATE',
    'FRAGMENT_MEGA'
);

-- Cada área usa uma linha evolutiva de referência. O estágio da missão define
-- qual material nomeado entra na pool, mantendo o material específico por Digimon.
INSERT INTO loot_table_entries (
    loot_table_id,
    rarity,
    item_type,
    material_code,
    weight,
    min_quantity,
    max_quantity,
    active
)
SELECT
    lt.id,
    CASE md.required_stage
        WHEN 'BABY' THEN 'COMMON'
        WHEN 'BABY_II' THEN 'COMMON'
        WHEN 'ROOKIE' THEN 'RARE'
        WHEN 'CHAMPION' THEN 'EPIC'
        WHEN 'ULTIMATE' THEN 'EPIC'
        WHEN 'MEGA' THEN 'LEGENDARY'
    END,
    'EVOLUTION_MATERIAL',
    CASE md.area
        WHEN 'NATIVE_FOREST' THEN CASE md.required_stage
            WHEN 'BABY' THEN 'FRAGMENT_KOROMON'
            WHEN 'BABY_II' THEN 'FRAGMENT_KOROMON'
            WHEN 'ROOKIE' THEN 'FRAGMENT_AGUMON'
            WHEN 'CHAMPION' THEN 'FRAGMENT_GREYMON'
            WHEN 'ULTIMATE' THEN 'FRAGMENT_METALGREYMON'
            WHEN 'MEGA' THEN 'FRAGMENT_WARGREYMON'
        END
        WHEN 'GEAR_SAVANNA' THEN CASE md.required_stage
            WHEN 'BABY' THEN 'FRAGMENT_TSUNOMON'
            WHEN 'BABY_II' THEN 'FRAGMENT_TSUNOMON'
            WHEN 'ROOKIE' THEN 'FRAGMENT_GABUMON'
            WHEN 'CHAMPION' THEN 'FRAGMENT_GARURUMON'
            WHEN 'ULTIMATE' THEN 'FRAGMENT_WEREGARURUMON'
            WHEN 'MEGA' THEN 'FRAGMENT_METALGARURUMON'
        END
        WHEN 'FACTORIAL_TOWN' THEN CASE md.required_stage
            WHEN 'BABY' THEN 'FRAGMENT_TOKOMON'
            WHEN 'BABY_II' THEN 'FRAGMENT_TOKOMON'
            WHEN 'ROOKIE' THEN 'FRAGMENT_PATAMON'
            WHEN 'CHAMPION' THEN 'FRAGMENT_ANGEMON'
            WHEN 'ULTIMATE' THEN 'FRAGMENT_MAGNAANGEMON'
            WHEN 'MEGA' THEN 'FRAGMENT_SERAPHIMON'
        END
        WHEN 'FREEZELAND' THEN CASE md.required_stage
            WHEN 'BABY' THEN 'FRAGMENT_YOKOMON'
            WHEN 'BABY_II' THEN 'FRAGMENT_YOKOMON'
            WHEN 'ROOKIE' THEN 'FRAGMENT_BIYOMON'
            WHEN 'CHAMPION' THEN 'FRAGMENT_BIRDRAMON'
            WHEN 'ULTIMATE' THEN 'FRAGMENT_GARUDAMON'
            WHEN 'MEGA' THEN 'FRAGMENT_PHOENIXMON'
        END
        WHEN 'SERVER_DESERT' THEN CASE md.required_stage
            WHEN 'BABY' THEN 'FRAGMENT_MOTIMON'
            WHEN 'BABY_II' THEN 'FRAGMENT_MOTIMON'
            WHEN 'ROOKIE' THEN 'FRAGMENT_TENTOMON'
            WHEN 'CHAMPION' THEN 'FRAGMENT_KABUTERIMON'
            WHEN 'ULTIMATE' THEN 'FRAGMENT_MEGAKABUTERIMON'
            WHEN 'MEGA' THEN 'FRAGMENT_HERCULESKABUTERIMON'
        END
        WHEN 'INFINITY_MOUNTAIN' THEN CASE md.required_stage
            WHEN 'BABY' THEN 'FRAGMENT_BUKAMON'
            WHEN 'BABY_II' THEN 'FRAGMENT_BUKAMON'
            WHEN 'ROOKIE' THEN 'FRAGMENT_GOMAMON'
            WHEN 'CHAMPION' THEN 'FRAGMENT_IKKAKUMON'
            WHEN 'ULTIMATE' THEN 'FRAGMENT_ZUDOMON'
            WHEN 'MEGA' THEN 'FRAGMENT_VIKEMON'
        END
    END,
    20,
    CASE md.required_stage
        WHEN 'BABY' THEN 1
        WHEN 'BABY_II' THEN 1
        WHEN 'ROOKIE' THEN 1
        WHEN 'CHAMPION' THEN 1
        WHEN 'ULTIMATE' THEN 1
        WHEN 'MEGA' THEN 1
    END,
    CASE md.required_stage
        WHEN 'BABY' THEN 5
        WHEN 'BABY_II' THEN 5
        WHEN 'ROOKIE' THEN 5
        WHEN 'CHAMPION' THEN 3
        WHEN 'ULTIMATE' THEN 3
        WHEN 'MEGA' THEN 2
    END,
    TRUE
FROM mission_definitions md
JOIN loot_tables lt ON lt.code = 'LOOT_TABLE_MISSION_' || md.id
WHERE md.area IS NOT NULL;

INSERT INTO item_definitions (
    code,
    name,
    description,
    category,
    stackable,
    buy_price,
    sell_price,
    tradable,
    sellable,
    usable,
    max_stack,
    rarity,
    icon
)
SELECT
    'CHEST_MISSION_' || md.id,
    'Baú ' || CASE md.area
        WHEN 'NATIVE_FOREST' THEN 'Floresta Nativa'
        WHEN 'GEAR_SAVANNA' THEN 'Gear Savanna'
        WHEN 'FACTORIAL_TOWN' THEN 'Factorial Town'
        WHEN 'FREEZELAND' THEN 'Freezeland'
        WHEN 'SERVER_DESERT' THEN 'Server Desert'
        WHEN 'INFINITY_MOUNTAIN' THEN 'Infinity Mountain'
    END || ' — ' || md.name,
    'Baú obtido ao concluir a missão ' || md.name || '.',
    'CHEST',
    TRUE,
    NULL,
    NULL,
    TRUE,
    TRUE,
    TRUE,
    999,
    'COMMON',
    'chest_mission_' || lower(md.id)
FROM mission_definitions md
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
    code,
    name,
    description,
    icon,
    loot_table_id,
    item_definition_id,
    tradable,
    active
)
SELECT
    'CHEST_MISSION_' || md.id,
    item.name,
    item.description,
    item.icon,
    lt.id,
    item.id,
    TRUE,
    TRUE
FROM mission_definitions md
JOIN loot_tables lt ON lt.code = 'LOOT_TABLE_MISSION_' || md.id
JOIN item_definitions item ON item.code = 'CHEST_MISSION_' || md.id
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active;

UPDATE mission_definitions md
SET chest_definition_id = cd.id
FROM chest_definitions cd
WHERE cd.code = 'CHEST_MISSION_' || md.id;
