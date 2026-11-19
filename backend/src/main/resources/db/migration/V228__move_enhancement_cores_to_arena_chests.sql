-- V228: corrige a V227 sem alterar uma migration já executada.
-- A V227 inseriu os núcleos nas tabelas de concessão da Arena. O jogador,
-- porém, recebe e abre os baús legados, cujas tabelas internas são corrigidas aqui.

-- Remove da elegibilidade as entradas inseridas incorretamente pela V227 nas
-- tabelas de concessão da Arena.
UPDATE loot_table_entries entry
SET active = FALSE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND entry.item_type = 'EVOLUTION_MATERIAL'
  AND entry.material_code IN (
      'BASIC_ENHANCEMENT_CORE',
      'ADVANCED_ENHANCEMENT_CORE',
      'SUPREME_ENHANCEMENT_CORE'
  )
  AND table_row.code IN (
      'LOOT_TABLE_ARENA_PRATA',
      'LOOT_TABLE_ARENA_PLATINA',
      'LOOT_TABLE_ARENA_DIAMANTE'
  );

-- Restaura os pesos originais das pools de concessão afetadas pela V227.
UPDATE loot_table_entries entry
SET weight = CASE
    WHEN table_row.code = 'LOOT_TABLE_ARENA_PRATA'
         AND entry.rarity = 'EPIC'
         AND entry.item_type = 'XP_DISC_5' THEN 50
    WHEN table_row.code = 'LOOT_TABLE_ARENA_PRATA'
         AND entry.rarity = 'EPIC'
         AND entry.item_type = 'LOOT_CHEST'
         AND entry.material_code = 'CHEST_FRAGMENT_ULTIMATE' THEN 50
    WHEN table_row.code = 'LOOT_TABLE_ARENA_PLATINA'
         AND entry.rarity = 'EPIC'
         AND entry.item_type = 'XP_DISC_5' THEN 1
    WHEN table_row.code = 'LOOT_TABLE_ARENA_PLATINA'
         AND entry.rarity = 'EPIC'
         AND entry.item_type = 'INCUBATOR_EPIC' THEN 1
    WHEN table_row.code = 'LOOT_TABLE_ARENA_PLATINA'
         AND entry.rarity = 'EPIC'
         AND entry.item_type = 'LOOT_CHEST'
         AND entry.material_code = 'CHEST_FRAGMENT_ULTIMATE' THEN 1
    WHEN table_row.code = 'LOOT_TABLE_ARENA_DIAMANTE'
         AND entry.rarity = 'LEGENDARY' THEN 1
    ELSE entry.weight
END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND (
      (table_row.code = 'LOOT_TABLE_ARENA_PRATA' AND entry.rarity = 'EPIC')
      OR (table_row.code = 'LOOT_TABLE_ARENA_PLATINA' AND entry.rarity = 'EPIC')
      OR (table_row.code = 'LOOT_TABLE_ARENA_DIAMANTE' AND entry.rarity = 'LEGENDARY')
  );

-- BAÚ LEGADO DE PRATA
-- EPIC atual: XP 60 / baú Ultimate 40.
-- Novo: XP 54 / baú Ultimate 36 / BASIC 10.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'BASIC_ENHANCEMENT_CORE',
       NULL, NULL, 10, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_CHEST_ARENA_PRATA'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'BASIC_ENHANCEMENT_CORE'
  );

UPDATE loot_table_entries entry
SET weight = CASE
    WHEN entry.item_type = 'XP_DISC_5' THEN 54
    WHEN entry.item_type = 'LOOT_CHEST'
         AND entry.material_code = 'CHEST_FRAGMENT_ULTIMATE' THEN 36
    ELSE entry.weight
END,
    active = TRUE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_CHEST_ARENA_PRATA'
  AND entry.rarity = 'EPIC';

-- BAÚ LEGADO DE PLATINA
-- EPIC atual: XP 45 / incubadora Epic 35 / baú Ultimate 20.
-- Novo: XP 38 / incubadora Epic 30 / baú Ultimate 17 / ADVANCED 15.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'ADVANCED_ENHANCEMENT_CORE',
       NULL, NULL, 15, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_CHEST_ARENA_PLATINA'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'ADVANCED_ENHANCEMENT_CORE'
  );

UPDATE loot_table_entries entry
SET weight = CASE
    WHEN entry.item_type = 'XP_DISC_5' THEN 38
    WHEN entry.item_type = 'INCUBATOR_EPIC' THEN 30
    WHEN entry.item_type = 'LOOT_CHEST'
         AND entry.material_code = 'CHEST_FRAGMENT_ULTIMATE' THEN 17
    ELSE entry.weight
END,
    active = TRUE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_CHEST_ARENA_PLATINA'
  AND entry.rarity = 'EPIC';

-- BAÚ LEGADO DE DIAMANTE
-- LEGENDARY atual: quatro entradas com peso 25.
-- Novo: quatro entradas existentes com peso 20 / SUPREME 20.
INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'LEGENDARY', 'EVOLUTION_MATERIAL', 'SUPREME_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_CHEST_ARENA_DIAMANTE'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'LEGENDARY'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'SUPREME_ENHANCEMENT_CORE'
  );

UPDATE loot_table_entries entry
SET weight = 20,
    active = TRUE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_CHEST_ARENA_DIAMANTE'
  AND entry.rarity = 'LEGENDARY'
  AND NOT (
      entry.item_type = 'EVOLUTION_MATERIAL'
      AND entry.material_code = 'SUPREME_ENHANCEMENT_CORE'
  );

-- Valida que os núcleos ficaram somente nos baús corretos e que as pools
-- alteradas somam exatamente 100.
DO $$
DECLARE
    invalid_pools INT;
    wrong_arena_entries INT;
BEGIN
    SELECT COUNT(*) INTO wrong_arena_entries
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    WHERE entry.active = TRUE
      AND entry.item_type = 'EVOLUTION_MATERIAL'
      AND entry.material_code IN (
          'BASIC_ENHANCEMENT_CORE',
          'ADVANCED_ENHANCEMENT_CORE',
          'SUPREME_ENHANCEMENT_CORE'
      )
      AND table_row.code IN (
          'LOOT_TABLE_ARENA_PRATA',
          'LOOT_TABLE_ARENA_PLATINA',
          'LOOT_TABLE_ARENA_DIAMANTE'
      );

    IF wrong_arena_entries > 0 THEN
        RAISE EXCEPTION 'Arena: % núcleo(s) permaneceram ativo(s) nas tabelas de concessão.', wrong_arena_entries;
    END IF;

    SELECT COUNT(*) INTO invalid_pools
    FROM (
        SELECT table_row.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND (
              (table_row.code = 'LOOT_TABLE_CHEST_ARENA_PRATA' AND entry.rarity = 'EPIC')
              OR (table_row.code = 'LOOT_TABLE_CHEST_ARENA_PLATINA' AND entry.rarity = 'EPIC')
              OR (table_row.code = 'LOOT_TABLE_CHEST_ARENA_DIAMANTE' AND entry.rarity = 'LEGENDARY')
          )
        GROUP BY table_row.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION 'Arena: % pool(s) de baús não somam 100.', invalid_pools;
    END IF;
END $$;

COMMIT;
