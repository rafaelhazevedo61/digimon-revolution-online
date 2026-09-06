BEGIN;

-- V213: alinha as Loot Tables dos bosses à planilha consolidada de drops.
-- V212 permanece imutável e é corrigida por reconstrução nesta migration.
-- Equipamentos T2-T5 não entram porque ainda não possuem representação
-- compatível em loot_table_entries.

DO $$
DECLARE
    missing_items INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_items
    FROM (
        SELECT DISTINCT seed.item_code
        FROM (VALUES
            ('REFINEMENT_STONE'), ('TRAINING_STONE'), ('DATA_CORE'),
            ('XP_DISC_1'), ('XP_DISC_3'), ('XP_DISC_5'),
            ('REFINEMENT_SUCCESS_BOOST'), ('REFINEMENT_PROTECTION'),
            ('ASCENSION_CORE'), ('RARITY_PRESERVATION'), ('RARITY_REROLL')
        ) AS seed(item_code)
        LEFT JOIN item_definitions item ON item.code = seed.item_code
        WHERE item.id IS NULL
    ) missing;

    IF missing_items > 0 THEN
        RAISE EXCEPTION
            'Bosses: % item(ns) não-equipment da planilha não encontrado(s) no catálogo.',
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
    ('NORMAL',  'COMMON', 70), ('NORMAL',  'RARE', 20), ('NORMAL',  'EPIC', 8),  ('NORMAL',  'LEGENDARY', 2),
    ('DAILY',   'COMMON', 60), ('DAILY',   'RARE', 28), ('DAILY',   'EPIC', 10), ('DAILY',   'LEGENDARY', 2),
    ('WEEKLY',  'COMMON', 45), ('WEEKLY',  'RARE', 30), ('WEEKLY',  'EPIC', 18), ('WEEKLY',  'LEGENDARY', 7),
    ('MONTHLY', 'COMMON', 30), ('MONTHLY', 'RARE', 30), ('MONTHLY', 'EPIC', 25), ('MONTHLY', 'LEGENDARY', 15)
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
    -- Boss normal: KUWAGAMON, DEVIMON, ETEMON_BOSS, PIEDMON.
    ('NORMAL',  'COMMON',    'REFINEMENT_STONE',       50, 1, 2),
    ('NORMAL',  'COMMON',    'XP_DISC_1',              50, 1, 1),
    ('NORMAL',  'RARE',      'TRAINING_STONE',          50, 2, 3),
    ('NORMAL',  'RARE',      'DATA_CORE',               50, 3, 5),
    ('NORMAL',  'EPIC',      'REFINEMENT_SUCCESS_BOOST',100, 1, 1),
    ('NORMAL',  'LEGENDARY', 'REFINEMENT_PROTECTION',  100, 1, 1),

    -- Boss diário: todos recebem a mesma composição por categoria.
    ('DAILY',   'COMMON',    'TRAINING_STONE',          50, 2, 3),
    ('DAILY',   'COMMON',    'XP_DISC_1',              50, 1, 1),
    ('DAILY',   'RARE',      'DATA_CORE',               50, 5, 8),
    ('DAILY',   'RARE',      'XP_DISC_3',              50, 1, 1),
    ('DAILY',   'EPIC',      'REFINEMENT_SUCCESS_BOOST',100, 1, 1),
    ('DAILY',   'LEGENDARY', 'REFINEMENT_PROTECTION',   50, 1, 1),
    ('DAILY',   'LEGENDARY', 'ASCENSION_CORE',          50, 1, 1),

    -- Boss semanal: todos recebem a mesma composição por categoria.
    ('WEEKLY',  'COMMON',    'TRAINING_STONE',          50, 4, 6),
    ('WEEKLY',  'COMMON',    'XP_DISC_3',              50, 1, 1),
    ('WEEKLY',  'RARE',      'REFINEMENT_SUCCESS_BOOST',100, 1, 1),
    ('WEEKLY',  'EPIC',      'REFINEMENT_PROTECTION',  100, 1, 1),
    ('WEEKLY',  'LEGENDARY', 'ASCENSION_CORE',         100, 1, 2),

    -- Boss mensal: todos recebem a mesma composição por categoria.
    ('MONTHLY', 'COMMON',    'TRAINING_STONE',          50, 8, 12),
    ('MONTHLY', 'COMMON',    'XP_DISC_5',              50, 1, 1),
    ('MONTHLY', 'RARE',      'REFINEMENT_SUCCESS_BOOST',100, 2, 3),
    ('MONTHLY', 'EPIC',      'REFINEMENT_PROTECTION',  100, 1, 1),
    ('MONTHLY', 'LEGENDARY', 'RARITY_PRESERVATION',    50, 1, 1),
    ('MONTHLY', 'LEGENDARY', 'RARITY_REROLL',           50, 1, 1)
) AS seed(boss_type, rarity, item_type, item_weight, min_quantity, max_quantity)
    ON seed.boss_type = boss.boss_type;

DO $$
DECLARE
    invalid_rarity_pools INT;
    invalid_item_pools INT;
    invalid_equipment_entries INT;
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
    INTO invalid_equipment_entries
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
      AND entry.item_type IN ('EQUIPMENT', 'EQUIPMENT_POOL');

    IF invalid_rarity_pools > 0 OR invalid_item_pools > 0 OR invalid_equipment_entries > 0 THEN
        RAISE EXCEPTION
            'Bosses: % tabela(s) de raridade, % pool(s) de itens ou % entrada(s) de equipamento inválida(s).',
            invalid_rarity_pools, invalid_item_pools, invalid_equipment_entries;
    END IF;
END $$;

COMMIT;
