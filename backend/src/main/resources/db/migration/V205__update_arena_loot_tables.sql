BEGIN;

-- Atualiza somente o conteúdo das cinco Loot Tables da Arena.
-- Os intervalos de abertura atuais são preservados:
-- Bronze/Prata 1-2, Ouro/Platina 1-3 e Diamante 1-4.
UPDATE loot_table_rarity_weights rw
SET weight = CASE lt.code
    WHEN 'LOOT_TABLE_ARENA_BRONZE' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 70
        WHEN 'RARE' THEN 20
        WHEN 'EPIC' THEN 8
        WHEN 'LEGENDARY' THEN 2
    END
    WHEN 'LOOT_TABLE_ARENA_PRATA' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 60
        WHEN 'RARE' THEN 28
        WHEN 'EPIC' THEN 10
        WHEN 'LEGENDARY' THEN 2
    END
    WHEN 'LOOT_TABLE_ARENA_OURO' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 50
        WHEN 'RARE' THEN 30
        WHEN 'EPIC' THEN 15
        WHEN 'LEGENDARY' THEN 5
    END
    WHEN 'LOOT_TABLE_ARENA_PLATINA' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 45
        WHEN 'RARE' THEN 30
        WHEN 'EPIC' THEN 18
        WHEN 'LEGENDARY' THEN 7
    END
    WHEN 'LOOT_TABLE_ARENA_DIAMANTE' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 35
        WHEN 'RARE' THEN 30
        WHEN 'EPIC' THEN 25
        WHEN 'LEGENDARY' THEN 10
    END
END
FROM loot_tables lt
WHERE rw.loot_table_id = lt.id
  AND lt.code IN (
      'LOOT_TABLE_ARENA_BRONZE',
      'LOOT_TABLE_ARENA_PRATA',
      'LOOT_TABLE_ARENA_OURO',
      'LOOT_TABLE_ARENA_PLATINA',
      'LOOT_TABLE_ARENA_DIAMANTE'
  );

-- Remove as entradas anteriores para impedir duplicidade de opções após a atualização.
DELETE FROM loot_table_entries
WHERE loot_table_id IN (
    SELECT id
    FROM loot_tables
    WHERE code IN (
        'LOOT_TABLE_ARENA_BRONZE',
        'LOOT_TABLE_ARENA_PRATA',
        'LOOT_TABLE_ARENA_OURO',
        'LOOT_TABLE_ARENA_PLATINA',
        'LOOT_TABLE_ARENA_DIAMANTE'
    )
);

-- Como a planilha não fornece pesos internos para a Arena, as entradas de cada
-- raridade recebem peso igual. O sorteador usa pesos relativos por raridade.
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
SELECT lt.id, seed.rarity, seed.item_type, seed.material_code, seed.weight,
       seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    -- Bronze: 70 / 20 / 8 / 2.
    ('LOOT_TABLE_ARENA_BRONZE', 'COMMON',    'XP_DISC_1',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'COMMON',    'INCUBATOR_COMMON', NULL,                1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'RARE',      'LOOT_CHEST',   'CHEST_DIGITAMA_RANDOM',  1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'EPIC',      'XP_DISC_3',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'LEGENDARY', 'STORAGE_SLOT_1', NULL,                 1, 1, 1),
    ('LOOT_TABLE_ARENA_BRONZE', 'LEGENDARY', 'INCUBATOR_RARE', NULL,                 1, 1, 1),

    -- Prata: 60 / 28 / 10 / 2.
    ('LOOT_TABLE_ARENA_PRATA', 'COMMON',    'XP_DISC_1',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'COMMON',    'INCUBATOR_COMMON', NULL,                1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'XP_DISC_3',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'LOOT_CHEST',   'CHEST_DIGITAMA_RANDOM',  1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'EPIC',      'XP_DISC_5',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'LEGENDARY', 'STORAGE_SLOT_1', NULL,                 1, 1, 1),
    ('LOOT_TABLE_ARENA_PRATA', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     1, 1, 1),

    -- Ouro: 50 / 30 / 15 / 5.
    ('LOOT_TABLE_ARENA_OURO', 'COMMON',    'XP_DISC_1',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'COMMON',    'INCUBATOR_COMMON', NULL,                1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'XP_DISC_3',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'RARE',      'INCUBATOR_RARE', NULL,                  1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'EPIC',      'XP_DISC_5',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'LEGENDARY', 'STORAGE_SLOT_5', NULL,                  1, 1, 1),
    ('LOOT_TABLE_ARENA_OURO', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     1, 1, 1),

    -- Platina: 45 / 30 / 18 / 7.
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    'XP_DISC_1',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    'INCUBATOR_COMMON', NULL,                1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'XP_DISC_3',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'INCUBATOR_RARE', NULL,                  1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'LOOT_CHEST',   'CHEST_DIGITAMA_RANDOM',  1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      'XP_DISC_5',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      'INCUBATOR_EPIC', NULL,                 1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 'XP_DISC_10',   NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 'STORAGE_SLOT_5', NULL,                  1, 1, 1),
    ('LOOT_TABLE_ARENA_PLATINA', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     1, 1, 1),

    -- Diamante: 35 / 30 / 25 / 10.
    ('LOOT_TABLE_ARENA_DIAMANTE', 'COMMON',    'XP_DISC_1',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'COMMON',    'INCUBATOR_COMMON', NULL,                1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'COMMON',    'LOOT_CHEST',   'CHEST_FRAGMENT_ROOKIE',  1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'XP_DISC_3',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'INCUBATOR_RARE', NULL,                  1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'RARE',      'LOOT_CHEST',   'CHEST_FRAGMENT_CHAMPION',1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'XP_DISC_5',    NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'INCUBATOR_EPIC', NULL,                 1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'EPIC',      'LOOT_CHEST',   'CHEST_FRAGMENT_ULTIMATE',1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'XP_DISC_10',   NULL,                    1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'STORAGE_SLOT_10', NULL,                1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'INCUBATOR_LEGENDARY', NULL,             1, 1, 1),
    ('LOOT_TABLE_ARENA_DIAMANTE', 'LEGENDARY', 'LOOT_CHEST',   'CHEST_FRAGMENT_MEGA',     1, 1, 1)
) AS seed(table_code, rarity, item_type, material_code, weight, min_quantity, max_quantity)
    ON seed.table_code = lt.code
WHERE lt.code IN (
    'LOOT_TABLE_ARENA_BRONZE',
    'LOOT_TABLE_ARENA_PRATA',
    'LOOT_TABLE_ARENA_OURO',
    'LOOT_TABLE_ARENA_PLATINA',
    'LOOT_TABLE_ARENA_DIAMANTE'
);

-- Falha cedo caso as Loot Tables ou itens/baús usados pela Arena não existam.
DO $$
DECLARE
    missing_tables INT;
    missing_items INT;
    missing_chests INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_tables
    FROM (VALUES
        ('LOOT_TABLE_ARENA_BRONZE'),
        ('LOOT_TABLE_ARENA_PRATA'),
        ('LOOT_TABLE_ARENA_OURO'),
        ('LOOT_TABLE_ARENA_PLATINA'),
        ('LOOT_TABLE_ARENA_DIAMANTE')
    ) AS required(code)
    WHERE NOT EXISTS (
        SELECT 1 FROM loot_tables lt WHERE lt.code = required.code
    );

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Arena: existem % Loot Table(s) sem definição.', missing_tables;
    END IF;

    SELECT COUNT(*)
    INTO missing_items
    FROM (VALUES
        ('XP_DISC_1'),
        ('XP_DISC_3'),
        ('XP_DISC_5'),
        ('XP_DISC_10'),
        ('INCUBATOR_COMMON'),
        ('INCUBATOR_RARE'),
        ('INCUBATOR_EPIC'),
        ('INCUBATOR_LEGENDARY'),
        ('STORAGE_SLOT_1'),
        ('STORAGE_SLOT_5'),
        ('STORAGE_SLOT_10')
    ) AS required(code)
    WHERE NOT EXISTS (
        SELECT 1 FROM item_definitions item WHERE item.code = required.code
    );

    IF missing_items > 0 THEN
        RAISE EXCEPTION 'Arena: existem % item(ns) simples sem definição no catálogo.', missing_items;
    END IF;

    SELECT COUNT(*)
    INTO missing_chests
    FROM (VALUES
        ('CHEST_DIGITAMA_RANDOM'),
        ('CHEST_FRAGMENT_ROOKIE'),
        ('CHEST_FRAGMENT_CHAMPION'),
        ('CHEST_FRAGMENT_ULTIMATE'),
        ('CHEST_FRAGMENT_MEGA')
    ) AS required(code)
    WHERE NOT EXISTS (
        SELECT 1 FROM chest_definitions chest WHERE chest.code = required.code
    );

    IF missing_chests > 0 THEN
        RAISE EXCEPTION 'Arena: existem % Baú(s) sem definição.', missing_chests;
    END IF;
END $$;

-- Confirma que todos os pesos principais continuam fechando em 100.
DO $$
DECLARE
    invalid_count INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_count
    FROM (
        SELECT lt.id
        FROM loot_table_rarity_weights rw
        JOIN loot_tables lt ON lt.id = rw.loot_table_id
        WHERE lt.code IN (
            'LOOT_TABLE_ARENA_BRONZE',
            'LOOT_TABLE_ARENA_PRATA',
            'LOOT_TABLE_ARENA_OURO',
            'LOOT_TABLE_ARENA_PLATINA',
            'LOOT_TABLE_ARENA_DIAMANTE'
        )
        GROUP BY lt.id
        HAVING SUM(rw.weight) <> 100
    ) AS invalid_tables;

    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Arena: soma de pesos de raridade diferente de 100.';
    END IF;
END $$;

COMMIT;
