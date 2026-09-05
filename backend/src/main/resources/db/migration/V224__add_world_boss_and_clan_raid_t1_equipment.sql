BEGIN;

-- V224: normaliza os equipamentos das recompensas especiais.
-- Chefe Mundial usa Overlord T1; Incursão de Clã usa Kernel T1.
-- A raridade da entrada continua sendo a raridade da pool. A raridade efetiva
-- do equipamento é sorteada na abertura porque equipment_rarity fica NULL.
CREATE TEMP TABLE special_boss_equipment_drops (
    table_code VARCHAR(100) NOT NULL,
    equipment_template_name VARCHAR(120) NOT NULL,
    equipment_entry_weight INT NOT NULL,
    legacy_item_type VARCHAR(50),
    legacy_item_weight INT
) ON COMMIT DROP;

INSERT INTO special_boss_equipment_drops (
    table_code, equipment_template_name, equipment_entry_weight, legacy_item_type, legacy_item_weight
)
VALUES
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'Lâmina do Overlord T1', 50, 'INCUBATOR_RARE', 50),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'Couraça do Overlord T1', 50, 'INCUBATOR_EPIC', 50),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'Coroa do Overlord T1',    50, 'INCUBATOR_EPIC', 50),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',       'Martelo Kernel T1',       50, 'REFINEMENT_PROTECTION', 50),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',    'Kernel Shell T1',         50, 'XP_DISC_10', 50),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW',    'Kernel Core T1',          100, NULL, NULL);

DO $$
DECLARE
    missing_tables INT;
    missing_templates INT;
BEGIN
    SELECT COUNT(*) INTO missing_tables
    FROM special_boss_equipment_drops expected
    LEFT JOIN loot_tables table_row ON table_row.code = expected.table_code
    WHERE table_row.id IS NULL;

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Recompensas especiais: % Loot Table(s) não encontrada(s).', missing_tables;
    END IF;

    SELECT COUNT(*) INTO missing_templates
    FROM special_boss_equipment_drops expected
    LEFT JOIN equipment_templates template
      ON template.name = expected.equipment_template_name
     AND template.active = TRUE
    WHERE template.name IS NULL;

    IF missing_templates > 0 THEN
        RAISE EXCEPTION 'Recompensas especiais: % template(s) Kernel/Overlord T1 não encontrado(s) ou inativo(s).', missing_templates;
    END IF;
END $$;

-- Se algum ambiente já tiver recebido uma entrada de equipamento diferente,
-- ela deixa de participar do sorteio; a peça abaixo passa a ser a referência
-- única e explícita de cada recompensa especial.
UPDATE loot_table_entries entry
SET active = FALSE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND entry.item_type = 'EQUIPMENT'
  AND (table_row.code LIKE 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_%'
       OR table_row.code LIKE 'LOOT_TABLE_CLAN_RAID_OMEGAMON_%')
  AND NOT EXISTS (
      SELECT 1
      FROM special_boss_equipment_drops expected
      WHERE expected.table_code = table_row.code
        AND expected.equipment_template_name = entry.equipment_template_name
  );

-- Mantém os drops existentes, mas divide a recompensa lendária entre o drop
-- legado e a peça T1 específica quando há um drop legado definido.
UPDATE loot_table_entries entry
SET weight = expected.legacy_item_weight,
    active = TRUE
FROM loot_tables table_row
JOIN special_boss_equipment_drops expected
  ON expected.table_code = table_row.code
 AND expected.legacy_item_type IS NOT NULL
WHERE entry.loot_table_id = table_row.id
  AND entry.rarity = 'LEGENDARY'
  AND entry.item_type = expected.legacy_item_type
  AND entry.item_type <> 'EQUIPMENT';

-- Reaplica uma entrada de equipamento já existente sem criar duplicatas.
UPDATE loot_table_entries entry
SET rarity = 'LEGENDARY',
    item_type = 'EQUIPMENT',
    material_code = NULL,
    equipment_template_name = expected.equipment_template_name,
    equipment_rarity = NULL,
    weight = expected.equipment_entry_weight,
    min_quantity = 1,
    max_quantity = 1,
    active = TRUE
FROM loot_tables table_row
JOIN special_boss_equipment_drops expected ON expected.table_code = table_row.code
WHERE entry.loot_table_id = table_row.id
  AND entry.item_type = 'EQUIPMENT'
  AND entry.equipment_template_name = expected.equipment_template_name;

INSERT INTO loot_table_entries (
    loot_table_id,
    rarity,
    item_type,
    material_code,
    equipment_template_name,
    equipment_rarity,
    weight,
    min_quantity,
    max_quantity,
    active
)
SELECT
    table_row.id,
    'LEGENDARY',
    'EQUIPMENT',
    NULL,
    expected.equipment_template_name,
    NULL,
    expected.equipment_entry_weight,
    1,
    1,
    TRUE
FROM special_boss_equipment_drops expected
JOIN loot_tables table_row ON table_row.code = expected.table_code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = table_row.id
      AND existing.item_type = 'EQUIPMENT'
      AND existing.equipment_template_name = expected.equipment_template_name
);

DO $$
DECLARE
    equipment_count INT;
    invalid_pools INT;
BEGIN
    SELECT COUNT(*) INTO equipment_count
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    WHERE entry.active = TRUE
      AND entry.rarity = 'LEGENDARY'
      AND entry.item_type = 'EQUIPMENT'
      AND (table_row.code LIKE 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_%'
           OR table_row.code LIKE 'LOOT_TABLE_CLAN_RAID_OMEGAMON_%');

    IF equipment_count <> 6 THEN
        RAISE EXCEPTION 'Recompensas especiais: esperadas 6 entradas de equipamento T1, encontradas %.', equipment_count;
    END IF;

    SELECT COUNT(*) INTO invalid_pools
    FROM (
        SELECT table_row.code
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND entry.rarity = 'LEGENDARY'
          AND (table_row.code LIKE 'LOOT_TABLE_BOSS_WORLD_APOCALYMON_%'
               OR table_row.code LIKE 'LOOT_TABLE_CLAN_RAID_OMEGAMON_%')
        GROUP BY table_row.code
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION 'Recompensas especiais: % pool(s) lendária(s) não somam 100.', invalid_pools;
    END IF;
END $$;

COMMIT;
