BEGIN;

ALTER TABLE arena_matches
    ADD COLUMN reward_chest_definition_id BIGINT REFERENCES chest_definitions(id);

CREATE INDEX idx_arena_matches_reward_chest_definition_id
    ON arena_matches (reward_chest_definition_id);

-- A Arena já possui cinco tiers calculados pelo rating. Cada tier recebe um
-- Baú próprio, negociável e sem expiração; a pool permanece administrativa.
INSERT INTO loot_tables (
    code,
    name,
    description,
    active,
    min_items,
    max_items
)
VALUES
    ('LOOT_TABLE_ARENA_BRONZE', 'Loot Table Baú Arena Bronze', 'Pool inicial conservadora do Baú Arena Bronze.', TRUE, 1, 2),
    ('LOOT_TABLE_ARENA_PRATA', 'Loot Table Baú Arena Prata', 'Pool inicial conservadora do Baú Arena Prata.', TRUE, 1, 2),
    ('LOOT_TABLE_ARENA_OURO', 'Loot Table Baú Arena Ouro', 'Pool inicial progressiva do Baú Arena Ouro.', TRUE, 1, 3),
    ('LOOT_TABLE_ARENA_PLATINA', 'Loot Table Baú Arena Platina', 'Pool inicial progressiva do Baú Arena Platina.', TRUE, 1, 3),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'Loot Table Baú Arena Diamante', 'Pool inicial progressiva do Baú Arena Diamante.', TRUE, 1, 4)
ON CONFLICT (code) DO NOTHING;

-- Os pesos são percentuais relativos entre as quatro raridades oficiais.
INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT lt.id, seed.rarity, seed.weight
FROM loot_tables lt
JOIN (VALUES
    ('LOOT_TABLE_ARENA_BRONZE',  'COMMON',    80),
    ('LOOT_TABLE_ARENA_BRONZE',  'RARE',      15),
    ('LOOT_TABLE_ARENA_BRONZE',  'EPIC',       4),
    ('LOOT_TABLE_ARENA_BRONZE',  'LEGENDARY',  1),
    ('LOOT_TABLE_ARENA_PRATA',   'COMMON',    65),
    ('LOOT_TABLE_ARENA_PRATA',   'RARE',      25),
    ('LOOT_TABLE_ARENA_PRATA',   'EPIC',       8),
    ('LOOT_TABLE_ARENA_PRATA',   'LEGENDARY',  2),
    ('LOOT_TABLE_ARENA_OURO',    'COMMON',    50),
    ('LOOT_TABLE_ARENA_OURO',    'RARE',      30),
    ('LOOT_TABLE_ARENA_OURO',    'EPIC',      15),
    ('LOOT_TABLE_ARENA_OURO',    'LEGENDARY',  5),
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    40),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      30),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      20),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 10),
    ('LOOT_TABLE_ARENA_DIAMANTE','COMMON',    30),
    ('LOOT_TABLE_ARENA_DIAMANTE','RARE',      30),
    ('LOOT_TABLE_ARENA_DIAMANTE','EPIC',      25),
    ('LOOT_TABLE_ARENA_DIAMANTE','LEGENDARY', 15)
) AS seed(table_code, rarity, weight) ON seed.table_code = lt.code
ON CONFLICT (loot_table_id, rarity) DO NOTHING;

-- As entradas usam apenas tipos oficiais. Materiais de evolução são nomeados
-- por material_code; os fragmentos genéricos legados não entram nas novas pools.
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
SELECT lt.id, seed.rarity, seed.item_type, seed.material_code,
       seed.weight, seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    -- Bronze: itens básicos, com raridades superiores ainda conservadoras.
    ('LOOT_TABLE_ARENA_BRONZE', 'COMMON',    'POTION_SMALL',      NULL,                  45, 1, 3),
    ('LOOT_TABLE_ARENA_BRONZE', 'COMMON',    'TRAINING_STONE',    NULL,                  35, 1, 3),
    ('LOOT_TABLE_ARENA_BRONZE', 'COMMON',    'DATA_CORE',         NULL,                  20, 1, 2),
    ('LOOT_TABLE_ARENA_BRONZE', 'RARE',      'DIGITAMA_STARTER',  NULL,                  60, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'RARE',      'DIGITAMA_NATURE',   NULL,                  40, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'EPIC',      'INCUBATOR_COMMON',  NULL,                 100, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'LEGENDARY', 'INCUBATOR_RARE',    NULL,                 100, 1, 1),

    -- Prata: digitamas, fragmentos nomeados iniciais e incubadoras.
    ('LOOT_TABLE_ARENA_PRATA', 'COMMON',    'POTION_SMALL',      NULL,                  35, 1, 4),
    ('LOOT_TABLE_ARENA_PRATA', 'COMMON',    'TRAINING_STONE',    NULL,                  35, 1, 4),
    ('LOOT_TABLE_ARENA_PRATA', 'COMMON',    'DATA_CORE',         NULL,                  30, 1, 3),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'DIGITAMA_FIRE',     NULL,                  25, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'DIGITAMA_WATER',    NULL,                  25, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'DIGITAMA_NATURE',   NULL,                  20, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'EVOLUTION_MATERIAL','FRAGMENT_AGUMON',     15, 1, 4),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'EVOLUTION_MATERIAL','FRAGMENT_GABUMON',    15, 1, 4),
    ('LOOT_TABLE_ARENA_PRATA', 'EPIC',      'INCUBATOR_COMMON',  NULL,                  60, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'EPIC',      'INCUBATOR_RARE',    NULL,                  40, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'LEGENDARY', 'INCUBATOR_EPIC',    NULL,                 100, 1, 1),

    -- Ouro: melhor acesso a materiais nomeados e incubadoras raras.
    ('LOOT_TABLE_ARENA_OURO', 'COMMON',    'TRAINING_STONE',    NULL,                  35, 1, 5),
    ('LOOT_TABLE_ARENA_OURO', 'COMMON',    'DATA_CORE',         NULL,                  35, 1, 4),
    ('LOOT_TABLE_ARENA_OURO', 'COMMON',    'REFINEMENT_STONE',   NULL,                  30, 1, 3),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'DIGITAMA_FIRE',     NULL,                  20, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'DIGITAMA_WATER',    NULL,                  20, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'DIGITAMA_NATURE',   NULL,                  20, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'EVOLUTION_MATERIAL','FRAGMENT_AGUMON',     20, 1, 5),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'EVOLUTION_MATERIAL','FRAGMENT_GABUMON',    20, 1, 5),
    ('LOOT_TABLE_ARENA_OURO', 'EPIC',      'INCUBATOR_RARE',    NULL,                  55, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'EPIC',      'EVOLUTION_MATERIAL','FRAGMENT_GREYMON',   45, 1, 4),
    ('LOOT_TABLE_ARENA_OURO', 'LEGENDARY', 'INCUBATOR_EPIC',    NULL,                  60, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'LEGENDARY', 'EVOLUTION_MATERIAL','FRAGMENT_WARGREYMON', 40, 1, 3),

    -- Platina: materiais Champion/Ultimate e melhor distribuição de raridade.
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    'DATA_CORE',          NULL,                  35, 1, 5),
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    'REFINEMENT_STONE',    NULL,                  35, 1, 4),
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    'TRAINING_STONE',      NULL,                  30, 1, 5),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_GREYMON',    25, 1, 5),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_GARURUMON',  25, 1, 5),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_ANGEMON',    25, 1, 5),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'INCUBATOR_RARE',      NULL,                  25, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_METALGREYMON',35, 1, 4),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_WEREGARURUMON',35, 1, 4),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      'INCUBATOR_EPIC',      NULL,                  30, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_WARGREYMON',  50, 1, 3),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_METALGARURUMON',30, 1, 3),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 'INCUBATOR_EPIC',      NULL,                  20, 1, 1),

    -- Diamante: materiais Mega e itens de maior raridade.
    ('LOOT_TABLE_ARENA_DIAMANTE', 'COMMON',    'DATA_CORE',          NULL,                  40, 1, 6),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'COMMON',    'REFINEMENT_STONE',    NULL,                  35, 1, 5),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'COMMON',    'TRAINING_STONE',      NULL,                  25, 1, 6),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_METALGREYMON',30, 1, 5),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_METALGARURUMON',25, 1, 5),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'EVOLUTION_MATERIAL', 'FRAGMENT_SERAPHIMON', 20, 1, 5),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'INCUBATOR_EPIC',     NULL,                  25, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_WARGREYMON',  25, 1, 4),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_METALGARURUMON',25, 1, 4),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'EVOLUTION_MATERIAL', 'FRAGMENT_PHOENIXMON',  20, 1, 4),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'INCUBATOR_EPIC',     NULL,                  30, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_SERAPHIMON',  25, 1, 3),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_PHOENIXMON',   25, 1, 3),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_HERCULESKABUTERIMON',25, 1, 3),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'EVOLUTION_MATERIAL', 'FRAGMENT_VIKEMON',     25, 1, 3)
) AS seed(table_code, rarity, item_type, material_code, weight, min_quantity, max_quantity)
    ON seed.table_code = lt.code;

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
VALUES
    ('CHEST_ARENA_BRONZE',  'Baú Arena Bronze',  'Baú recebido por vitórias no tier Bronze da Arena.',  'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_arena_bronze'),
    ('CHEST_ARENA_PRATA',   'Baú Arena Prata',   'Baú recebido por vitórias no tier Prata da Arena.',   'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_arena_prata'),
    ('CHEST_ARENA_OURO',    'Baú Arena Ouro',    'Baú recebido por vitórias no tier Ouro da Arena.',    'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_arena_ouro'),
    ('CHEST_ARENA_PLATINA', 'Baú Arena Platina', 'Baú recebido por vitórias no tier Platina da Arena.', 'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_arena_platina'),
    ('CHEST_ARENA_DIAMANTE','Baú Arena Diamante','Baú recebido por vitórias no tier Diamante da Arena.','CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'COMMON', 'chest_arena_diamante')
ON CONFLICT (code) DO NOTHING;

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
SELECT seed.chest_code,
       seed.name,
       seed.description,
       seed.icon,
       lt.id,
       item.id,
       TRUE,
       TRUE
FROM (VALUES
    ('CHEST_ARENA_BRONZE',   'Baú Arena Bronze',   'Baú recebido por vitórias no tier Bronze da Arena.',   'chest_arena_bronze',   'LOOT_TABLE_ARENA_BRONZE'),
    ('CHEST_ARENA_PRATA',    'Baú Arena Prata',    'Baú recebido por vitórias no tier Prata da Arena.',    'chest_arena_prata',    'LOOT_TABLE_ARENA_PRATA'),
    ('CHEST_ARENA_OURO',     'Baú Arena Ouro',     'Baú recebido por vitórias no tier Ouro da Arena.',     'chest_arena_ouro',     'LOOT_TABLE_ARENA_OURO'),
    ('CHEST_ARENA_PLATINA',  'Baú Arena Platina',  'Baú recebido por vitórias no tier Platina da Arena.',  'chest_arena_platina',  'LOOT_TABLE_ARENA_PLATINA'),
    ('CHEST_ARENA_DIAMANTE', 'Baú Arena Diamante', 'Baú recebido por vitórias no tier Diamante da Arena.', 'chest_arena_diamante', 'LOOT_TABLE_ARENA_DIAMANTE')
) AS seed(chest_code, name, description, icon, loot_table_code)
JOIN loot_tables lt ON lt.code = seed.loot_table_code
JOIN item_definitions item ON item.code = seed.chest_code
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active;

COMMIT;
