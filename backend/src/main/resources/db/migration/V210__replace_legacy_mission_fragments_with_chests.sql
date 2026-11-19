BEGIN;

-- V210: substitui recompensas diretas de fragmentos legados por baús.
-- V208 e V209 permanecem imutáveis; esta migration altera somente a
-- representação das recompensas legadas nas Loot Tables de Missões.
--
-- Os fragmentos individuais continuam válidos dentro das Loot Tables dos
-- próprios baús (LOOT_TABLE_SHOP_FRAGMENT_*). O escopo abaixo é restrito às
-- tabelas LOOT_TABLE_MISSION_%.

DO $$
DECLARE
    missing_chests INT;
    invalid_legacy_entries INT;
    invalid_pools INT;
BEGIN
    SELECT COUNT(*)
    INTO missing_chests
    FROM (
        SELECT DISTINCT mapped.chest_code
        FROM (
            VALUES
                ('FRAGMENT_BABY_II', 'CHEST_FRAGMENT_BABY_II'),
                ('FRAGMENT_ROOKIE',  'CHEST_FRAGMENT_ROOKIE'),
                ('FRAGMENT_CHAMPION','CHEST_FRAGMENT_CHAMPION'),
                ('FRAGMENT_ULTIMATE','CHEST_FRAGMENT_ULTIMATE'),
                ('FRAGMENT_MEGA',    'CHEST_FRAGMENT_MEGA')
        ) AS mapped(legacy_code, chest_code)
        LEFT JOIN item_definitions chest_item
          ON chest_item.code = mapped.chest_code
        WHERE chest_item.id IS NULL
    ) missing;

    IF missing_chests > 0 THEN
        RAISE EXCEPTION
            'Missões: % baú(s) de fragmentos não encontrado(s) no catálogo.',
            missing_chests;
    END IF;
END $$;

UPDATE loot_table_entries entry
SET item_type = 'LOOT_CHEST',
    material_code = CASE COALESCE(NULLIF(entry.material_code, ''), entry.item_type)
        WHEN 'FRAGMENT_BABY_II'  THEN 'CHEST_FRAGMENT_BABY_II'
        WHEN 'FRAGMENT_ROOKIE'   THEN 'CHEST_FRAGMENT_ROOKIE'
        WHEN 'FRAGMENT_CHAMPION' THEN 'CHEST_FRAGMENT_CHAMPION'
        WHEN 'FRAGMENT_ULTIMATE' THEN 'CHEST_FRAGMENT_ULTIMATE'
        WHEN 'FRAGMENT_MEGA'     THEN 'CHEST_FRAGMENT_MEGA'
    END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code LIKE 'LOOT_TABLE_MISSION_%'
  AND entry.active = TRUE
  AND COALESCE(NULLIF(entry.material_code, ''), entry.item_type) IN (
      'FRAGMENT_BABY_II',
      'FRAGMENT_ROOKIE',
      'FRAGMENT_CHAMPION',
      'FRAGMENT_ULTIMATE',
      'FRAGMENT_MEGA'
  );

DO $$
DECLARE
    invalid_legacy_entries INT;
    invalid_pools INT;
BEGIN
    SELECT COUNT(*)
    INTO invalid_legacy_entries
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    WHERE table_row.code LIKE 'LOOT_TABLE_MISSION_%'
      AND entry.active = TRUE
      AND COALESCE(NULLIF(entry.material_code, ''), entry.item_type) IN (
          'FRAGMENT_BABY_II',
          'FRAGMENT_ROOKIE',
          'FRAGMENT_CHAMPION',
          'FRAGMENT_ULTIMATE',
          'FRAGMENT_MEGA'
      );

    IF invalid_legacy_entries > 0 THEN
        RAISE EXCEPTION
            'Missões: % recompensa(s) de fragmento legado permaneceram ativas.',
            invalid_legacy_entries;
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
            'Missões: soma de pesos internos diferente de 100 em % pool(s) após a V210.',
            invalid_pools;
    END IF;
END $$;

COMMIT;
