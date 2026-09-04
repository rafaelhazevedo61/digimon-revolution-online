BEGIN;

-- Rebalanceamento dos pesos internos por item conforme a proposta revisada.
-- Os pesos de raridade, quantidades e vínculos dos baús não são alterados.
UPDATE loot_table_entries entry
SET weight = seed.weight
FROM loot_tables lt
JOIN (VALUES
    -- Bronze
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'COMMON',    'XP_DISC_1',    NULL,                    55),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'COMMON',    'INCUBATOR_COMMON', NULL,                30),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  15),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'RARE',      'LOOT_CHEST',   'CHEST_DIGITAMA_RANDOM',  70),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',30),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'EPIC',      'XP_DISC_3',    NULL,                    65),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',35),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LEGENDARY', 'STORAGE_SLOT_1', NULL,                 70),
    ('LOOT_TABLE_CHEST_ARENA_BRONZE', 'LEGENDARY', 'INCUBATOR_RARE', NULL,                 30),

    -- Prata
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'COMMON',    'XP_DISC_1',    NULL,                    55),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'COMMON',    'INCUBATOR_COMMON', NULL,                25),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  20),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'RARE',      'XP_DISC_3',    NULL,                    50),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'RARE',      'LOOT_CHEST',   'CHEST_DIGITAMA_RANDOM',  30),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',20),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'EPIC',      'XP_DISC_5',    NULL,                    60),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',40),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LEGENDARY', 'STORAGE_SLOT_1', NULL,                 60),
    ('LOOT_TABLE_CHEST_ARENA_PRATA', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',    40),

    -- Ouro
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'COMMON',    'XP_DISC_1',    NULL,                    50),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'COMMON',    'INCUBATOR_COMMON', NULL,                30),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  20),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'RARE',      'XP_DISC_3',    NULL,                    45),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'RARE',      'INCUBATOR_RARE', NULL,                  55),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'EPIC',      'XP_DISC_5',    NULL,                    60),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',40),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'LEGENDARY', 'STORAGE_SLOT_5', NULL,                  55),
    ('LOOT_TABLE_CHEST_ARENA_OURO', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     45),

    -- Platina
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'COMMON',    'XP_DISC_1',    NULL,                    45),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'COMMON',    'INCUBATOR_COMMON', NULL,                30),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  25),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'RARE',      'XP_DISC_3',    NULL,                    40),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'RARE',      'INCUBATOR_RARE', NULL,                  30),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'RARE',      'LOOT_CHEST',   'CHEST_DIGITAMA_RANDOM',  15),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',15),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'EPIC',      'XP_DISC_5',    NULL,                    45),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'EPIC',      'INCUBATOR_EPIC', NULL,                 35),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',20),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LEGENDARY', 'XP_DISC_10',   NULL,                    30),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LEGENDARY', 'STORAGE_SLOT_5', NULL,                  40),
    ('LOOT_TABLE_CHEST_ARENA_PLATINA', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     30),

    -- Diamante
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'COMMON',    'XP_DISC_1',    NULL,                    40),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'COMMON',    'INCUBATOR_COMMON', NULL,                30),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  30),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'RARE',      'XP_DISC_3',    NULL,                    35),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'RARE',      'INCUBATOR_RARE', NULL,                  30),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',35),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'EPIC',      'XP_DISC_5',    NULL,                    35),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'EPIC',      'INCUBATOR_EPIC', NULL,                 35),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',30),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LEGENDARY', 'XP_DISC_10',   NULL,                    25),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LEGENDARY', 'STORAGE_SLOT_10', NULL,                25),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LEGENDARY', 'INCUBATOR_LEGENDARY', NULL,             25),
    ('LOOT_TABLE_CHEST_ARENA_DIAMANTE', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     25)
) AS seed(table_code, rarity, item_type, material_code, weight)
    ON seed.table_code = lt.code
WHERE entry.loot_table_id = lt.id
  AND entry.rarity = seed.rarity
  AND entry.item_type = seed.item_type
  AND entry.material_code IS NOT DISTINCT FROM seed.material_code;

-- Confirma que os pesos internos de cada raridade fecham em 100.
DO $$
DECLARE
    invalid_count INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_count
    FROM (
        SELECT lt.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables lt ON lt.id = entry.loot_table_id
        WHERE lt.code IN (
            'LOOT_TABLE_CHEST_ARENA_BRONZE',
            'LOOT_TABLE_CHEST_ARENA_PRATA',
            'LOOT_TABLE_CHEST_ARENA_OURO',
            'LOOT_TABLE_CHEST_ARENA_PLATINA',
            'LOOT_TABLE_CHEST_ARENA_DIAMANTE'
        )
          AND entry.active = TRUE
        GROUP BY lt.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) AS invalid_pools;

    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Arena: soma de pesos internos diferente de 100 em % pool(s).', invalid_count;
    END IF;
END $$;

COMMIT;
