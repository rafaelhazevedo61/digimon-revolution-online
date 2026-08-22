BEGIN;

ALTER TABLE boss_definitions
    ADD COLUMN chest_definition_id BIGINT REFERENCES chest_definitions(id);

CREATE INDEX idx_boss_definitions_chest_definition_id
    ON boss_definitions (chest_definition_id);

-- Cada Boss normal/periódico recebe uma Loot Table própria. A tabela pode ser
-- reutilizada posteriormente por outro Boss através do painel administrativo.
INSERT INTO loot_tables (code, name, description, active, min_items, max_items)
SELECT
    CASE b.boss_type
        WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || b.code
        WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || b.code
        WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || b.code
        WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || b.code
    END,
    CASE b.boss_type
        WHEN 'NORMAL' THEN 'Loot Table Baú Boss — ' || b.name
        WHEN 'DAILY' THEN 'Loot Table Baú Boss Diário — ' || b.name
        WHEN 'WEEKLY' THEN 'Loot Table Baú Boss Semanal — ' || b.name
        WHEN 'MONTHLY' THEN 'Loot Table Baú Boss Mensal — ' || b.name
    END,
    'Pool inicial do Baú de recompensa do Boss ' || b.name || '. Itens diretos legados foram migrados para esta tabela.',
    TRUE,
    1,
    1
FROM boss_definitions b
WHERE b.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
ON CONFLICT (code) DO NOTHING;

-- A Opção B começa com a chance inteira na raridade COMMON. As entradas
-- continuam editáveis no painel, e as raridades sem entrada ativa são
-- ignoradas pelo sorteador atual.
INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT
    lt.id,
    rarity.rarity,
    rarity.weight
FROM loot_tables lt
CROSS JOIN (VALUES
    ('COMMON', 100),
    ('RARE', 0),
    ('EPIC', 0),
    ('LEGENDARY', 0)
) AS rarity(rarity, weight)
WHERE lt.code LIKE 'LOOT_TABLE_BOSS_%'
ON CONFLICT (loot_table_id, rarity) DO NOTHING;

-- Migra os drops diretos de itens catalogados mantendo chance e quantidade
-- como peso e range da entrada da Loot Table. Equipment permanece no fluxo
-- legado e não é copiado para esta tabela.
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
    'COMMON',
    bd.item_code,
    NULL,
    GREATEST(bd.chance, 1),
    bd.min_quantity,
    bd.max_quantity,
    TRUE
FROM boss_drops bd
JOIN boss_definitions b ON b.id = bd.boss_id
JOIN loot_tables lt ON lt.code = CASE b.boss_type
    WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || b.code
    WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || b.code
    WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || b.code
    WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || b.code
END
WHERE b.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
  AND bd.drop_type = 'ITEM'
  AND bd.item_code IN (
      'POTION_SMALL',
      'TRAINING_STONE',
      'DATA_CORE',
      'DIGITAMA_STARTER',
      'DIGITAMA_FIRE',
      'DIGITAMA_WATER',
      'DIGITAMA_NATURE',
      'INCUBATOR_COMMON',
      'INCUBATOR_RARE',
      'INCUBATOR_EPIC',
      'FRAGMENT_ROOKIE',
      'FRAGMENT_CHAMPION',
      'FRAGMENT_ULTIMATE',
      'FRAGMENT_MEGA',
      'REFINEMENT_STONE'
  );

-- Alguns Bosses diários não possuíam item direto no modelo anterior. Eles
-- ainda precisam entregar um Baú abrível; a entrada conservadora serve como
-- fallback inicial e deve ser balanceada pelo painel posteriormente.
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
    'COMMON',
    'POTION_SMALL',
    NULL,
    1,
    1,
    1,
    TRUE
FROM loot_tables lt
WHERE lt.code LIKE 'LOOT_TABLE_BOSS_%'
  AND NOT EXISTS (
      SELECT 1
      FROM loot_table_entries entry
      WHERE entry.loot_table_id = lt.id
  );

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
    CASE b.boss_type
        WHEN 'NORMAL' THEN 'CHEST_BOSS_' || b.code
        WHEN 'DAILY' THEN 'CHEST_BOSS_DAILY_' || b.code
        WHEN 'WEEKLY' THEN 'CHEST_BOSS_WEEKLY_' || b.code
        WHEN 'MONTHLY' THEN 'CHEST_BOSS_MONTHLY_' || b.code
    END,
    CASE b.boss_type
        WHEN 'NORMAL' THEN 'Baú Boss — ' || b.name
        WHEN 'DAILY' THEN 'Baú Boss Diário — ' || b.name
        WHEN 'WEEKLY' THEN 'Baú Boss Semanal — ' || b.name
        WHEN 'MONTHLY' THEN 'Baú Boss Mensal — ' || b.name
    END,
    'Baú recebido ao vencer o Boss ' || b.name || '.',
    'CHEST',
    TRUE,
    NULL,
    NULL,
    TRUE,
    TRUE,
    TRUE,
    999,
    'COMMON',
    'chest_boss_' || lower(b.code)
FROM boss_definitions b
WHERE b.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
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
SELECT
    CASE b.boss_type
        WHEN 'NORMAL' THEN 'CHEST_BOSS_' || b.code
        WHEN 'DAILY' THEN 'CHEST_BOSS_DAILY_' || b.code
        WHEN 'WEEKLY' THEN 'CHEST_BOSS_WEEKLY_' || b.code
        WHEN 'MONTHLY' THEN 'CHEST_BOSS_MONTHLY_' || b.code
    END,
    CASE b.boss_type
        WHEN 'NORMAL' THEN 'Baú Boss — ' || b.name
        WHEN 'DAILY' THEN 'Baú Boss Diário — ' || b.name
        WHEN 'WEEKLY' THEN 'Baú Boss Semanal — ' || b.name
        WHEN 'MONTHLY' THEN 'Baú Boss Mensal — ' || b.name
    END,
    'Baú recebido ao vencer o Boss ' || b.name || '.',
    'chest_boss_' || lower(b.code),
    lt.id,
    item.id,
    TRUE,
    TRUE
FROM boss_definitions b
JOIN loot_tables lt ON lt.code = CASE b.boss_type
    WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || b.code
    WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || b.code
    WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || b.code
    WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || b.code
END
JOIN item_definitions item ON item.code = CASE b.boss_type
    WHEN 'NORMAL' THEN 'CHEST_BOSS_' || b.code
    WHEN 'DAILY' THEN 'CHEST_BOSS_DAILY_' || b.code
    WHEN 'WEEKLY' THEN 'CHEST_BOSS_WEEKLY_' || b.code
    WHEN 'MONTHLY' THEN 'CHEST_BOSS_MONTHLY_' || b.code
END
WHERE b.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
ON CONFLICT (code) DO NOTHING;

UPDATE boss_definitions b
SET chest_definition_id = cd.id
FROM chest_definitions cd
WHERE cd.code = CASE b.boss_type
    WHEN 'NORMAL' THEN 'CHEST_BOSS_' || b.code
    WHEN 'DAILY' THEN 'CHEST_BOSS_DAILY_' || b.code
    WHEN 'WEEKLY' THEN 'CHEST_BOSS_WEEKLY_' || b.code
    WHEN 'MONTHLY' THEN 'CHEST_BOSS_MONTHLY_' || b.code
END
  AND b.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY');

COMMIT;

-- V107 cria o vínculo de origem. O inventário continua usando a definição
-- catalogada e o item_type LOOT_CHEST, como nos Baús de Missão.
