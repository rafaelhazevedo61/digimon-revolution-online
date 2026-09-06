BEGIN;

-- V216: alinha as três Loot Tables do World Boss ao mapeamento consolidado.
-- V110 e V215 permanecem imutáveis. Equipamentos e materiais de forja,
-- incluindo os avançados, ficam fora desta composição.

DO $$
DECLARE
    missing_items INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_items
    FROM (
        SELECT DISTINCT seed.item_code
        FROM (VALUES
            ('REFINEMENT_STONE'), ('DATA_CORE'),
            ('XP_DISC_1'), ('XP_DISC_3'), ('XP_DISC_5'), ('XP_DISC_10'),
            ('REFINEMENT_SUCCESS_BOOST'), ('REFINEMENT_PROTECTION'),
            ('ASCENSION_CORE')
        ) AS seed(item_code)
        LEFT JOIN item_definitions item ON item.code = seed.item_code
        WHERE item.id IS NULL
    ) missing;

    IF missing_items > 0 THEN
        RAISE EXCEPTION
            'World Boss: % item(ns) do mapeamento não encontrado(s) no catálogo.',
            missing_items;
    END IF;
END $$;

DELETE FROM loot_table_entries entry
USING loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code IN (
      'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
      'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
      'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
  );

UPDATE loot_table_rarity_weights weights
SET weight = seed.weight
FROM loot_tables table_row
JOIN (VALUES
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'COMMON',    65),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'RARE',      25),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'EPIC',       8),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'LEGENDARY',  2),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    35),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      30),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      25),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 10),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON',    45),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',      30),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'EPIC',      20),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'LEGENDARY',  5)
) AS seed(table_code, rarity, weight) ON seed.table_code = table_row.code
WHERE weights.loot_table_id = table_row.id
  AND weights.rarity = seed.rarity;

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT lt.id, seed.rarity, seed.item_type, NULL,
       seed.weight, seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    -- Baú de tentativa: Common 65 / Rare 25 / Epic 8 / Legendary 2.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'REFINEMENT_STONE',        50, 2, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'DATA_CORE',               30, 3, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'COMMON',    'XP_DISC_1',                20, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'XP_DISC_3',                50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'RARE',      'REFINEMENT_STONE',          50, 4, 5),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'EPIC',      'REFINEMENT_SUCCESS_BOOST',100, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT', 'LEGENDARY', 'REFINEMENT_PROTECTION',   100, 1, 1),

    -- Baú de maior dano: Common 35 / Rare 30 / Epic 25 / Legendary 10.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'COMMON',    'REFINEMENT_STONE',        100, 8, 12),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'REFINEMENT_SUCCESS_BOOST', 50, 2, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'RARE',      'XP_DISC_5',                50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      'REFINEMENT_PROTECTION',    50, 1, 2),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'EPIC',      'ASCENSION_CORE',            50, 2, 3),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'LEGENDARY', 'XP_DISC_10',              100, 1, 1),

    -- Baú de golpe final: Common 45 / Rare 30 / Epic 20 / Legendary 5.
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'COMMON', 'REFINEMENT_STONE',        100, 5, 7),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',   'XP_DISC_5',                 50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'RARE',   'REFINEMENT_SUCCESS_BOOST',  50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'EPIC',   'REFINEMENT_PROTECTION',     50, 1, 1),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'EPIC',   'ASCENSION_CORE',             50, 1, 1)
) AS seed(table_code, rarity, item_type, weight, min_quantity, max_quantity)
    ON seed.table_code = lt.code;

DO $$
DECLARE
    invalid_rarity_pools INT;
    invalid_item_pools INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_rarity_pools
    FROM (
        SELECT lt.code
        FROM loot_table_rarity_weights weights
        JOIN loot_tables lt ON lt.id = weights.loot_table_id
        WHERE lt.code IN (
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
        )
        GROUP BY lt.code
        HAVING SUM(weights.weight) <> 100
    ) invalid;

    SELECT COUNT(*)
    INTO invalid_item_pools
    FROM (
        SELECT lt.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables lt ON lt.id = entry.loot_table_id
        WHERE lt.code IN (
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE',
            'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
        )
          AND entry.active = TRUE
        GROUP BY lt.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;


    IF invalid_rarity_pools > 0 OR invalid_item_pools > 0 THEN
        RAISE EXCEPTION
            'World Boss: % pool(s) de raridade ou % pool(s) internas inválida(s).',
            invalid_rarity_pools, invalid_item_pools;
    END IF;
END $$;

COMMIT;
