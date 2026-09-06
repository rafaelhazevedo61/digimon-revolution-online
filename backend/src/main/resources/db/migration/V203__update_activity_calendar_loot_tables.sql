BEGIN;

-- O catálogo do projeto já define este item no ambiente alvo. O INSERT com
-- upsert também mantém a migration reproduzível em ambientes limpos.
INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES (
    'REFINEMENT_SUCCESS_BOOST',
    'Pergaminho de Refinamento',
    'Aumenta em 10 pontos percentuais a chance de sucesso de um refinamento.',
    'MATERIAL', TRUE, NULL, NULL, TRUE, TRUE, FALSE, 999, 'RARE',
    'refinement_success_boost'
)
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

-- Atualiza a quantidade de itens por abertura conforme a planilha:
-- Diário: 1 sorteio.
-- Mensal: 3 sorteios sem repetição.
UPDATE loot_tables
SET min_items = CASE code
                    WHEN 'LOOT_TABLE_ACTIVITY_CALENDAR' THEN 1
                    WHEN 'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY' THEN 3
                    ELSE min_items
                END,
    max_items = CASE code
                    WHEN 'LOOT_TABLE_ACTIVITY_CALENDAR' THEN 1
                    WHEN 'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY' THEN 3
                    ELSE max_items
                END,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code IN (
    'LOOT_TABLE_ACTIVITY_CALENDAR',
    'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY'
);

-- Evita duplicação quando a migration/semente anterior já criou entradas.
DELETE FROM loot_table_entries
WHERE loot_table_id IN (
    SELECT id
    FROM loot_tables
    WHERE code IN (
        'LOOT_TABLE_ACTIVITY_CALENDAR',
        'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY'
    )
);

-- Os pesos de raridade são os pesos principais da planilha.
UPDATE loot_table_rarity_weights rw
SET weight = CASE lt.code
    WHEN 'LOOT_TABLE_ACTIVITY_CALENDAR' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 70
        WHEN 'RARE' THEN 20
        WHEN 'EPIC' THEN 8
        WHEN 'LEGENDARY' THEN 2
    END
    WHEN 'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY' THEN CASE rw.rarity
        WHEN 'COMMON' THEN 30
        WHEN 'RARE' THEN 30
        WHEN 'EPIC' THEN 25
        WHEN 'LEGENDARY' THEN 15
    END
END
FROM loot_tables lt
WHERE rw.loot_table_id = lt.id
  AND lt.code IN (
      'LOOT_TABLE_ACTIVITY_CALENDAR',
      'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY'
  );

-- A planilha fornece percentuais dos itens dentro de cada raridade.
-- Como o sorteador usa pesos relativos dentro da raridade, os valores abaixo
-- preservam diretamente esses percentuais relativos.
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
SELECT lt.id, seed.rarity, seed.item_type, NULL, seed.weight,
       seed.min_quantity, seed.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    -- Calendário diário: Common 70 / Rare 20 / Epic 8 / Legendary 2.
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'COMMON',    'TRAINING_STONE',             20,  2,  3),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'COMMON',    'REFINEMENT_STONE',            20,  1,  2),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'COMMON',    'DATA_CORE',                   15,  2,  4),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'COMMON',    'XP_DISC_1',                    15,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'RARE',      'XP_DISC_3',                     8,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'RARE',      'COLLECTION_DIGIVICE',           8,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'RARE',      'REFINEMENT_SUCCESS_BOOST',      4,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'EPIC',      'XP_DISC_5',                     5,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'EPIC',      'COLLECTION_DIGIVICE',           3,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'LEGENDARY', 'RARITY_PRESERVATION',           1,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR', 'LEGENDARY', 'RARITY_REROLL',                 1,  1,  1),

    -- Calendário mensal: Common 30 / Rare 30 / Epic 25 / Legendary 15.
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'COMMON',    'REFINEMENT_STONE',            15,  6, 10),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'COMMON',    'DATA_CORE',                    10, 10, 15),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'COMMON',    'XP_DISC_5',                      5,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'RARE',      'XP_DISC_10',                    10,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'RARE',      'COLLECTION_DIGIVICE',           10,  2,  2),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'RARE',      'REFINEMENT_SUCCESS_BOOST',      10,  2,  2),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'EPIC',      'RARITY_PRESERVATION',           12,  2,  2),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'EPIC',      'CODE_INFINITE',                  8,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'EPIC',      'ASCENSION_CORE',                 5,  2,  2),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'LEGENDARY', 'RARITY_REROLL',                  5,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'LEGENDARY', 'RARITY_PRESERVATION',            5,  3,  3),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'LEGENDARY', 'XP_DISC_20',                     3,  1,  1),
    ('LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY', 'LEGENDARY', 'INCUBATOR_LEGENDARY',             2,  1,  1)
) AS seed(table_code, rarity, item_type, weight, min_quantity, max_quantity)
    ON seed.table_code = lt.code
WHERE lt.code IN (
    'LOOT_TABLE_ACTIVITY_CALENDAR',
    'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY'
);

-- Falha cedo caso algum item da configuração não exista no catálogo oficial.
DO $$
DECLARE
    missing_count INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_count
    FROM (
        VALUES
            ('TRAINING_STONE'),
            ('REFINEMENT_STONE'),
            ('DATA_CORE'),
            ('XP_DISC_1'),
            ('XP_DISC_3'),
            ('XP_DISC_5'),
            ('XP_DISC_10'),
            ('XP_DISC_20'),
            ('COLLECTION_DIGIVICE'),
            ('REFINEMENT_SUCCESS_BOOST'),
            ('RARITY_PRESERVATION'),
            ('RARITY_REROLL'),
            ('CODE_INFINITE'),
            ('ASCENSION_CORE'),
            ('INCUBATOR_LEGENDARY')
    ) AS required(code)
    WHERE NOT EXISTS (
        SELECT 1
        FROM item_definitions item
        WHERE item.code = required.code
    );

    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Calendário: existem % item(ns) sem definição no catálogo.', missing_count;
    END IF;
END $$;

-- Garante que nenhuma tabela fique com uma soma de pesos divergente de 100.
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
            'LOOT_TABLE_ACTIVITY_CALENDAR',
            'LOOT_TABLE_ACTIVITY_CALENDAR_MONTHLY'
        )
        GROUP BY lt.id
        HAVING SUM(rw.weight) <> 100
    ) AS invalid_tables;

    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Calendário: soma de pesos de raridade diferente de 100.';
    END IF;
END $$;

COMMIT;
