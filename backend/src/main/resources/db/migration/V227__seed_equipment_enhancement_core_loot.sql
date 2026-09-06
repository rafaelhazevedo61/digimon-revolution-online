-- V227: adiciona fontes de núcleos de aprimoramento às atividades existentes.
-- As pools de raridade não são alteradas; somente os pesos internos das raridades
-- selecionadas são redistribuídos para manter cada pool somando 100.

-- MISSÕES: MISSION_6 é a fonte inicial de BASIC_ENHANCEMENT_CORE.
-- Pool LEGENDARY atual: XP 34 / DATA_CORE 33 / equipamento 33.
UPDATE loot_table_entries entry
SET weight = CASE entry.item_type
    WHEN 'XP_DISC_1' THEN 25
    WHEN 'DATA_CORE' THEN 25
    WHEN 'EQUIPMENT' THEN 25
    ELSE entry.weight
END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_MISSION_MISSION_6'
  AND entry.rarity = 'LEGENDARY'
  AND (
      entry.item_type IN ('XP_DISC_1', 'DATA_CORE')
      OR (entry.item_type = 'EQUIPMENT' AND entry.equipment_template_name = 'Lança Firewall T1')
  );

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'LEGENDARY', 'EVOLUTION_MATERIAL', 'BASIC_ENHANCEMENT_CORE',
       NULL, NULL, 25, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_6'
  AND NOT EXISTS (
      SELECT 1
      FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'LEGENDARY'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'BASIC_ENHANCEMENT_CORE'
  );

-- ARENA: Prata fornece básico, Platina fornece avançado e Diamante fornece supremo.
-- Prata / EPIC: XP 45 / baú Ultimate 45 / BASIC 10.
UPDATE loot_table_entries entry
SET weight = CASE
    WHEN entry.item_type = 'XP_DISC_5' THEN 45
    WHEN entry.item_type = 'LOOT_CHEST' AND entry.material_code = 'CHEST_FRAGMENT_ULTIMATE' THEN 45
    ELSE entry.weight
END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_ARENA_PRATA'
  AND entry.rarity = 'EPIC';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'BASIC_ENHANCEMENT_CORE',
       NULL, NULL, 10, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_ARENA_PRATA'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'BASIC_ENHANCEMENT_CORE'
  );

-- Platina / EPIC: XP 30 / incubadora 30 / baú Ultimate 25 / ADVANCED 15.
UPDATE loot_table_entries entry
SET weight = CASE
    WHEN entry.item_type = 'XP_DISC_5' THEN 30
    WHEN entry.item_type = 'INCUBATOR_EPIC' THEN 30
    WHEN entry.item_type = 'LOOT_CHEST' AND entry.material_code = 'CHEST_FRAGMENT_ULTIMATE' THEN 25
    ELSE entry.weight
END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_ARENA_PLATINA'
  AND entry.rarity = 'EPIC';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'ADVANCED_ENHANCEMENT_CORE',
       NULL, NULL, 15, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_ARENA_PLATINA'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'ADVANCED_ENHANCEMENT_CORE'
  );

-- Diamante / LEGENDARY: XP 20 / storage 20 / incubadora 20 / baú Mega 20 / SUPREME 20.
UPDATE loot_table_entries entry
SET weight = 20
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_ARENA_DIAMANTE'
  AND entry.rarity = 'LEGENDARY';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'LEGENDARY', 'EVOLUTION_MATERIAL', 'SUPREME_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_ARENA_DIAMANTE'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'LEGENDARY'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'SUPREME_ENHANCEMENT_CORE'
  );

-- CHEFE MUNDIAL: tentativa = básico, maior dano = avançado, golpe final = supremo.
-- Tentativa / COMMON: refinement 45 / DATA_CORE 25 / XP 20 / BASIC 10.
UPDATE loot_table_entries entry
SET weight = CASE entry.item_type
    WHEN 'REFINEMENT_STONE' THEN 45
    WHEN 'DATA_CORE' THEN 25
    WHEN 'XP_DISC_1' THEN 20
    ELSE entry.weight
END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT'
  AND entry.rarity = 'COMMON';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'COMMON', 'EVOLUTION_MATERIAL', 'BASIC_ENHANCEMENT_CORE',
       NULL, NULL, 10, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'COMMON'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'BASIC_ENHANCEMENT_CORE'
  );

-- Maior dano / EPIC: protection 40 / ascension 40 / ADVANCED 20.
UPDATE loot_table_entries entry
SET weight = 40
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE'
  AND entry.rarity = 'EPIC'
  AND entry.item_type IN ('REFINEMENT_PROTECTION', 'ASCENSION_CORE');

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'ADVANCED_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'ADVANCED_ENHANCEMENT_CORE'
  );

-- Golpe final / LEGENDARY: equipamento Overlord 80 / SUPREME 20.
UPDATE loot_table_entries entry
SET weight = 80
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
  AND entry.rarity = 'LEGENDARY'
  AND entry.item_type = 'EQUIPMENT';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'LEGENDARY', 'EVOLUTION_MATERIAL', 'SUPREME_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'LEGENDARY'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'SUPREME_ENHANCEMENT_CORE'
  );

-- INCURSÃO: tentativa = básico, maior dano = avançado, golpe final = supremo.
-- Tentativa / COMMON: refinement 40 / DATA_CORE 20 / XP 20 / BASIC 20.
UPDATE loot_table_entries entry
SET weight = CASE entry.item_type
    WHEN 'REFINEMENT_STONE' THEN 40
    WHEN 'DATA_CORE' THEN 20
    WHEN 'XP_DISC_1' THEN 20
    ELSE entry.weight
END
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT'
  AND entry.rarity = 'COMMON';

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'COMMON', 'EVOLUTION_MATERIAL', 'BASIC_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'COMMON'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'BASIC_ENHANCEMENT_CORE'
  );

-- Maior dano / EPIC: protection 40 / ascension 40 / ADVANCED 20.
UPDATE loot_table_entries entry
SET weight = 40
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE'
  AND entry.rarity = 'EPIC'
  AND entry.item_type IN ('REFINEMENT_PROTECTION', 'ASCENSION_CORE');

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'ADVANCED_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'ADVANCED_ENHANCEMENT_CORE'
  );

-- Golpe final / EPIC: protection 40 / ascension 40 / SUPREME 20.
UPDATE loot_table_entries entry
SET weight = 40
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW'
  AND entry.rarity = 'EPIC'
  AND entry.item_type IN ('REFINEMENT_PROTECTION', 'ASCENSION_CORE');

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code,
    equipment_template_name, equipment_rarity, weight,
    min_quantity, max_quantity, active
)
SELECT table_row.id, 'EPIC', 'EVOLUTION_MATERIAL', 'SUPREME_ENHANCEMENT_CORE',
       NULL, NULL, 20, 1, 1, TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW'
  AND NOT EXISTS (
      SELECT 1 FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'EPIC'
        AND entry.item_type = 'EVOLUTION_MATERIAL'
        AND entry.material_code = 'SUPREME_ENHANCEMENT_CORE'
  );

-- Valida o catálogo e as pools alteradas antes de concluir a migração.
DO $$
DECLARE
    missing_catalog INT;
    invalid_pools INT;
BEGIN
    SELECT COUNT(*) INTO missing_catalog
    FROM (VALUES
        ('BASIC_ENHANCEMENT_CORE'),
        ('ADVANCED_ENHANCEMENT_CORE'),
        ('SUPREME_ENHANCEMENT_CORE')
    ) AS required(code)
    WHERE NOT EXISTS (
        SELECT 1 FROM item_definitions item WHERE item.code = required.code
    );

    IF missing_catalog > 0 THEN
        RAISE EXCEPTION 'Núcleos de aprimoramento ausentes no catálogo: %.', missing_catalog;
    END IF;

    SELECT COUNT(*) INTO invalid_pools
    FROM (
        SELECT table_row.code, entry.rarity
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND (
              (table_row.code = 'LOOT_TABLE_MISSION_MISSION_6' AND entry.rarity = 'LEGENDARY')
              OR (table_row.code = 'LOOT_TABLE_ARENA_PRATA' AND entry.rarity = 'EPIC')
              OR (table_row.code = 'LOOT_TABLE_ARENA_PLATINA' AND entry.rarity = 'EPIC')
              OR (table_row.code = 'LOOT_TABLE_ARENA_DIAMANTE' AND entry.rarity = 'LEGENDARY')
              OR (table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT' AND entry.rarity = 'COMMON')
              OR (table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE' AND entry.rarity = 'EPIC')
              OR (table_row.code = 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW' AND entry.rarity = 'LEGENDARY')
              OR (table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT' AND entry.rarity = 'COMMON')
              OR (table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE' AND entry.rarity = 'EPIC')
              OR (table_row.code = 'LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW' AND entry.rarity = 'EPIC')
          )
        GROUP BY table_row.code, entry.rarity
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION 'Loot de núcleos: % pool(s) não somam 100.', invalid_pools;
    END IF;
END $$;

COMMIT;
