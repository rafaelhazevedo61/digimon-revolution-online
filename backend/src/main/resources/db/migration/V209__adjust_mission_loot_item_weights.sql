BEGIN;

-- V209: ajusta os pesos internos dos drops das missões.
-- A V208 já foi executada e permanece imutável; esta migration corrige apenas
-- a distribuição entre itens dentro da mesma raridade.
--
-- Regra-base da referência:
--   - Digitama/incubadora: mesma categoria -> 50/50 após normalização;
--   - recompensas de progressão específicas: mesma categoria -> 50/50;
--   - XP Disc de entrada (60) versus núcleo de dados (30): 67/33 após normalização.
-- A composição da V208 contém 48 entradas abrangidas por estes ajustes.

DO $$
DECLARE
    updated_rows INT;
    invalid_pools INT;
BEGIN
    UPDATE loot_table_entries entry
    SET weight = CASE
        WHEN entry.item_type = 'LOOT_CHEST'
             AND entry.material_code = 'CHEST_DIGITAMA_RANDOM'
            THEN 50
        WHEN entry.item_type IN ('INCUBATOR_COMMON', 'STORAGE_SLOT_1', 'CODE_INFINITE')
            THEN 50
        WHEN entry.item_type = 'XP_DISC_1'
             AND EXISTS (
                 SELECT 1
                 FROM loot_table_entries paired
                 WHERE paired.loot_table_id = entry.loot_table_id
                   AND paired.rarity = entry.rarity
                   AND paired.item_type = 'DATA_CORE'
                   AND paired.active = TRUE
             )
            THEN 67
        WHEN entry.item_type = 'DATA_CORE'
             AND EXISTS (
                 SELECT 1
                 FROM loot_table_entries paired
                 WHERE paired.loot_table_id = entry.loot_table_id
                   AND paired.rarity = entry.rarity
                   AND paired.item_type = 'XP_DISC_1'
                   AND paired.active = TRUE
             )
            THEN 33
        ELSE entry.weight
    END
    FROM loot_tables table_row
    WHERE entry.loot_table_id = table_row.id
      AND table_row.code LIKE 'LOOT_TABLE_MISSION_%'
      AND entry.active = TRUE
      AND (
          (entry.item_type = 'LOOT_CHEST'
           AND entry.material_code = 'CHEST_DIGITAMA_RANDOM')
          OR entry.item_type IN ('INCUBATOR_COMMON', 'STORAGE_SLOT_1', 'CODE_INFINITE')
          OR (
              entry.item_type IN ('XP_DISC_1', 'DATA_CORE')
              AND EXISTS (
                  SELECT 1
                  FROM loot_table_entries paired
                  WHERE paired.loot_table_id = entry.loot_table_id
                    AND paired.rarity = entry.rarity
                    AND paired.item_type IN ('XP_DISC_1', 'DATA_CORE')
                    AND paired.item_type <> entry.item_type
                    AND paired.active = TRUE
              )
          )
      );

    GET DIAGNOSTICS updated_rows = ROW_COUNT;

    IF updated_rows <> 48 THEN
        RAISE EXCEPTION
            'Missões: esperadas 48 entradas atualizadas na V209, mas % foram encontradas.',
            updated_rows;
    END IF;

    SELECT COUNT(*)
    INTO invalid_pools
    FROM (
        SELECT table_row.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE table_row.code LIKE 'LOOT_TABLE_MISSION_%'
          AND entry.active = TRUE
        GROUP BY table_row.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION
            'Missões: soma de pesos internos diferente de 100 em % pool(s) após a V209.',
            invalid_pools;
    END IF;

    SELECT COUNT(*)
    INTO invalid_pools
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    WHERE table_row.code LIKE 'LOOT_TABLE_MISSION_%'
      AND entry.active = TRUE
      AND (
          (entry.item_type = 'LOOT_CHEST'
           AND entry.material_code = 'CHEST_DIGITAMA_RANDOM'
           AND entry.weight <> 50)
          OR (entry.item_type IN ('INCUBATOR_COMMON', 'STORAGE_SLOT_1', 'CODE_INFINITE')
              AND entry.weight <> 50)
          OR (entry.item_type = 'XP_DISC_1'
              AND EXISTS (
                  SELECT 1
                  FROM loot_table_entries paired
                  WHERE paired.loot_table_id = entry.loot_table_id
                    AND paired.rarity = entry.rarity
                    AND paired.item_type = 'DATA_CORE'
                    AND paired.active = TRUE
              )
              AND entry.weight <> 67)
          OR (entry.item_type = 'DATA_CORE'
              AND EXISTS (
                  SELECT 1
                  FROM loot_table_entries paired
                  WHERE paired.loot_table_id = entry.loot_table_id
                    AND paired.rarity = entry.rarity
                    AND paired.item_type = 'XP_DISC_1'
                    AND paired.active = TRUE
              )
              AND entry.weight <> 33)
      );

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION
            'Missões: % entrada(s) ficaram com peso interno incorreto após a V209.',
            invalid_pools;
    END IF;
END $$;

COMMIT;
