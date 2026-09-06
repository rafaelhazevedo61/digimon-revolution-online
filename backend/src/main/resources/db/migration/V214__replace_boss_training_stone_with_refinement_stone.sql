BEGIN;

-- V214: corrige a nomenclatura das pedras nas Loot Tables de bosses.
-- A planilha consolidada usa Pedra/Pedra de Refinamento; o código canônico
-- correspondente é REFINEMENT_STONE. V212 e V213 permanecem imutáveis.

UPDATE loot_table_entries entry
SET item_type = 'REFINEMENT_STONE'
FROM loot_tables table_row
JOIN boss_definitions boss ON table_row.code = CASE boss.boss_type
    WHEN 'NORMAL' THEN 'LOOT_TABLE_BOSS_NORMAL_' || boss.code
    WHEN 'DAILY' THEN 'LOOT_TABLE_BOSS_DAILY_' || boss.code
    WHEN 'WEEKLY' THEN 'LOOT_TABLE_BOSS_WEEKLY_' || boss.code
    WHEN 'MONTHLY' THEN 'LOOT_TABLE_BOSS_MONTHLY_' || boss.code
END
WHERE entry.loot_table_id = table_row.id
  AND boss.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
  AND entry.item_type = 'TRAINING_STONE';

DO $$
DECLARE
    remaining_training_stones INT;
    missing_refinement_stone INT;
BEGIN
    SELECT COUNT(*)
    INTO remaining_training_stones
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
      AND entry.item_type = 'TRAINING_STONE';

    SELECT COUNT(*)
    INTO missing_refinement_stone
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
      AND entry.item_type = 'REFINEMENT_STONE'
      AND NOT EXISTS (
          SELECT 1 FROM item_definitions item WHERE item.code = 'REFINEMENT_STONE'
      );

    IF remaining_training_stones > 0 OR missing_refinement_stone > 0 THEN
        RAISE EXCEPTION
            'Bosses: % TRAINING_STONE restante(s) e % entrada(s) sem REFINEMENT_STONE no catálogo.',
            remaining_training_stones, missing_refinement_stone;
    END IF;
END $$;

COMMIT;
