-- População inicial mínima para validar o fluxo dos baús no ambiente de testes.
-- Os pesos são iguais de propósito e podem ser alterados pelo painel Admin.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight, min_quantity, max_quantity, active
)
SELECT lt.id, seed.rarity, seed.item_type, NULL, 100, 1, 1, TRUE
FROM loot_tables lt
JOIN (VALUES
    ('LOOT_TABLE_ACTIVITY_CALENDAR',         'COMMON',    'TRAINING_STONE'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR',         'RARE',      'XP_DISC_5'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR',         'EPIC',      'XP_DISC_10'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR',         'LEGENDARY', 'XP_DISC_20'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'COMMON',    'DATA_CORE'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'RARE',      'XP_DISC_10'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'EPIC',      'CODE_INFINITE'),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'LEGENDARY', 'XP_DISC_20')
) AS seed(table_code, rarity, item_type) ON seed.table_code = lt.code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = lt.id
      AND existing.rarity = seed.rarity
      AND existing.item_type = seed.item_type
      AND existing.material_code IS NULL
);
