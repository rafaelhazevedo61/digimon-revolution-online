BEGIN;

-- V212: reconstrói as Loot Tables dos bosses NORMAL, DAILY, WEEKLY e MONTHLY.
-- O World Boss permanece fora do escopo. Equipamentos continuam no fluxo legado
-- enquanto o catálogo de loot não possui um tipo de equipamento compatível.

DO $$
DECLARE
    missing_items INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_items
    FROM (
        SELECT DISTINCT seed.item_code
        FROM (VALUES
            ('REFINEMENT_STONE'), ('POTION_SMALL'), ('TRAINING_STONE'),
            ('DATA_CORE'), ('CODE_INFINITE'), ('DIGITAMA_STARTER'),
            ('DIGITAMA_FIRE'), ('INCUBATOR_COMMON'), ('INCUBATOR_RARE'),
            ('INCUBATOR_EPIC'), ('INCUBATOR_LEGENDARY'), ('XP_DISC_5'),
            ('XP_DISC_10')
        ) AS seed(item_code)
        LEFT JOIN item_definitions item ON item.code = seed.item_code
        WHERE item.id IS NULL
    ) missing;

    IF missing_items > 0 THEN
        RAISE EXCEPTION
            'Bosses: % item(ns) canônico(s) não encontrado(s) no catálogo.',
            missing_items;
    END IF;
END $$;

DELETE FROM loot_table_entries entry
USING loot_tables table_row
JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
    WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
    WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
    WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
    WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
END
WHERE entry.loot_table_id = table_row.id
  AND boss.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY');

UPDATE loot_table_rarity_weights weights
SET weight = profile.weight
FROM loot_tables table_row
JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
    WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
    WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
    WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
    WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
END
JOIN (VALUES
    ('NORMAL',  'COMMON', 65), ('NORMAL',  'RARE', 22), ('NORMAL',  'EPIC', 10), ('NORMAL',  'LEGENDARY', 3),
    ('DAILY',   'COMMON', 55), ('DAILY',   'RARE', 28), ('DAILY',   'EPIC', 13), ('DAILY',   'LEGENDARY', 4),
    ('WEEKLY',  'COMMON', 40), ('WEEKLY',  'RARE', 30), ('WEEKLY',  'EPIC', 20), ('WEEKLY',  'LEGENDARY', 10),
    ('MONTHLY', 'COMMON', 20), ('MONTHLY', 'RARE', 30), ('MONTHLY', 'EPIC', 30), ('MONTHLY', 'LEGENDARY', 20)
) AS profile(boss_type, rarity, weight) ON profile.boss_type = boss.boss_type
WHERE weights.loot_table_id = table_row.id
  AND weights.rarity = profile.rarity;

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT
    table_row.id,
    seed.rarity,
    seed.item_type,
    NULL,
    seed.item_weight,
    seed.min_quantity,
    seed.max_quantity,
    TRUE
FROM loot_tables table_row
JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
    WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
    WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
    WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
    WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
END
JOIN (VALUES
    ('NORMAL',  'COMMON',    'REFINEMENT_STONE',     50, 1, 2),
    ('NORMAL',  'COMMON',    'POTION_SMALL',         30, 1, 2),
    ('NORMAL',  'COMMON',    'DATA_CORE',            20, 1, 1),
    ('NORMAL',  'RARE',      'DIGITAMA_STARTER',     60, 1, 1),
    ('NORMAL',  'RARE',      'INCUBATOR_COMMON',     40, 1, 1),
    ('NORMAL',  'EPIC',      'INCUBATOR_RARE',      100, 1, 1),
    ('NORMAL',  'LEGENDARY', 'INCUBATOR_EPIC',      100, 1, 1),

    ('DAILY',   'COMMON',    'REFINEMENT_STONE',     45, 1, 3),
    ('DAILY',   'COMMON',    'POTION_SMALL',         30, 1, 3),
    ('DAILY',   'COMMON',    'DATA_CORE',            25, 1, 2),
    ('DAILY',   'RARE',      'DIGITAMA_STARTER',     50, 1, 1),
    ('DAILY',   'RARE',      'INCUBATOR_COMMON',     50, 1, 1),
    ('DAILY',   'EPIC',      'INCUBATOR_RARE',      100, 1, 1),
    ('DAILY',   'LEGENDARY', 'INCUBATOR_EPIC',      100, 1, 1),

    ('WEEKLY',  'COMMON',    'REFINEMENT_STONE',     40, 2, 5),
    ('WEEKLY',  'COMMON',    'TRAINING_STONE',       35, 1, 4),
    ('WEEKLY',  'COMMON',    'DATA_CORE',            25, 1, 3),
    ('WEEKLY',  'RARE',      'INCUBATOR_COMMON',     50, 1, 1),
    ('WEEKLY',  'RARE',      'DIGITAMA_FIRE',        50, 1, 1),
    ('WEEKLY',  'EPIC',      'INCUBATOR_RARE',       60, 1, 1),
    ('WEEKLY',  'EPIC',      'XP_DISC_5',            40, 1, 1),
    ('WEEKLY',  'LEGENDARY', 'INCUBATOR_EPIC',      100, 1, 1),

    ('MONTHLY', 'COMMON',    'DATA_CORE',            40, 2, 5),
    ('MONTHLY', 'COMMON',    'REFINEMENT_STONE',     35, 3, 8),
    ('MONTHLY', 'COMMON',    'CODE_INFINITE',        25, 1, 2),
    ('MONTHLY', 'RARE',      'INCUBATOR_RARE',       50, 1, 1),
    ('MONTHLY', 'RARE',      'DIGITAMA_FIRE',        50, 1, 1),
    ('MONTHLY', 'EPIC',      'INCUBATOR_EPIC',       60, 1, 1),
    ('MONTHLY', 'EPIC',      'XP_DISC_10',           40, 1, 1),
    ('MONTHLY', 'LEGENDARY', 'INCUBATOR_LEGENDARY', 100, 1, 1)
) AS seed(boss_type, rarity, item_type, item_weight, min_quantity, max_quantity)
    ON seed.boss_type = boss.boss_type;

DO $$
DECLARE
    invalid_rarity_pools INT;
    invalid_item_pools INT;
    remaining_legacy INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_rarity_pools
    FROM (
        SELECT table_row.code
        FROM loot_table_rarity_weights weights
        JOIN loot_tables table_row ON table_row.id = weights.loot_table_id
        JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
            WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
            WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
            WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
            WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
        END
        WHERE boss.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
        GROUP BY table_row.code
        HAVING SUM(weights.weight) <> 100
    ) invalid;

    SELECT COUNT(*)
    INTO invalid_item_pools
    FROM (
        SELECT table_row.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
            WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
            WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
            WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
            WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
        END
        WHERE boss.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
          AND entry.active = TRUE
        GROUP BY table_row.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    SELECT COUNT(*)
    INTO remaining_legacy
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
        WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
        WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
        WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
        WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
    END
    WHERE boss.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
      AND entry.active = TRUE
      AND entry.item_type IN ('FRAGMENT_ROOKIE', 'FRAGMENT_CHAMPION', 'FRAGMENT_ULTIMATE', 'FRAGMENT_MEGA');

    IF invalid_rarity_pools > 0 OR invalid_item_pools > 0 THEN
        RAISE EXCEPTION
            'Bosses: % tabela(s) com pesos de raridade inválidos e % pool(s) de itens inválidos.',
            invalid_rarity_pools, invalid_item_pools;
    END IF;

    IF remaining_legacy > 0 THEN
        RAISE EXCEPTION
            'Bosses: % fragmento(s) genérico(s) legado(s) permaneceram nas Loot Tables.',
            remaining_legacy;
    END IF;
END $$;

COMMIT;
