-- Adiciona Discos de XP aos baús das missões das áreas anteriores.
-- A Infinity Mountain permanece fora desta migration para configuração manual.
-- Todas as missões de uma mesma área compartilham o mesmo pool de progressão.

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
    seed.rarity,
    seed.item_type,
    NULL,
    seed.weight,
    seed.min_quantity,
    seed.max_quantity,
    TRUE
FROM mission_definitions md
JOIN loot_tables lt
    ON lt.code = 'LOOT_TABLE_MISSION_' || md.id
JOIN (
    VALUES
        -- Native Forest: progressão inicial.
        ('NATIVE_FOREST',   'COMMON',    'XP_DISC_1', 12, 1, 2),
        ('NATIVE_FOREST',   'RARE',      'XP_DISC_3',  8, 1, 1),
        ('NATIVE_FOREST',   'EPIC',      'XP_DISC_5',  4, 1, 1),

        -- Gear Savanna: progressão inicial avançada.
        ('GEAR_SAVANNA',    'COMMON',    'XP_DISC_1', 10, 1, 2),
        ('GEAR_SAVANNA',    'RARE',      'XP_DISC_3', 10, 1, 1),
        ('GEAR_SAVANNA',    'EPIC',      'XP_DISC_5',  5, 1, 1),

        -- Factorial Town: progressão intermediária.
        ('FACTORIAL_TOWN',  'COMMON',    'XP_DISC_1',  8, 1, 2),
        ('FACTORIAL_TOWN',  'RARE',      'XP_DISC_3', 10, 1, 1),
        ('FACTORIAL_TOWN',  'EPIC',      'XP_DISC_5',  8, 1, 1),

        -- Freezeland: progressão intermediária avançada.
        ('FREEZELAND',      'COMMON',    'XP_DISC_3',  8, 1, 1),
        ('FREEZELAND',      'RARE',      'XP_DISC_5', 10, 1, 1),
        ('FREEZELAND',      'EPIC',      'XP_DISC_10', 5, 1, 1),

        -- Server Desert: última área antes do endgame.
        ('SERVER_DESERT',   'COMMON',    'XP_DISC_3',  8, 1, 1),
        ('SERVER_DESERT',   'RARE',      'XP_DISC_5', 10, 1, 1),
        ('SERVER_DESERT',   'EPIC',      'XP_DISC_10', 8, 1, 1)
) AS seed(area, rarity, item_type, weight, min_quantity, max_quantity)
    ON seed.area = md.area
WHERE md.area IN (
    'NATIVE_FOREST',
    'GEAR_SAVANNA',
    'FACTORIAL_TOWN',
    'FREEZELAND',
    'SERVER_DESERT'
)
AND NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = lt.id
      AND existing.rarity = seed.rarity
      AND existing.item_type = seed.item_type
      AND existing.material_code IS NULL
);

-- Arena permanece sem alteração nesta migration. A recomendação por tier
-- fica documentada no PR para configuração posterior, caso seja aprovada.
-- Infinity Mountain permanece sem alteração para montagem manual da loot table.

COMMENT ON TABLE loot_table_entries IS
    'Entradas de loot por raridade; Discos de XP de missões anteriores ao endgame são semeados em V141.';
      
-- A migration não altera os pesos de raridade das loot tables. Os pesos acima
-- são relativos apenas entre entradas da mesma raridade.
