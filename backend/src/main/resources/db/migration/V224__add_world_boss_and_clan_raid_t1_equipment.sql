BEGIN;

-- V224: adiciona uma peça T1 específica às pools lendárias do Chefe Mundial
-- e da Incursão, dividindo 100 pontos entre os drops que já existem e o novo
-- equipamento. A quantidade de entradas é contada no próprio banco.
CREATE TEMP TABLE special_boss_equipment_drops (
    table_code VARCHAR(100) NOT NULL,
    equipment_template_name VARCHAR(120) NOT NULL
) ON COMMIT DROP;

INSERT INTO special_boss_equipment_drops (table_code, equipment_template_name)
VALUES
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT',    'Lâmina do Overlord T1'),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE', 'Couraça do Overlord T1'),
    ('LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW', 'Coroa do Overlord T1'),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_ATTEMPT',       'Martelo Kernel T1'),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_TOP_DAMAGE',    'Kernel Shell T1'),
    ('LOOT_TABLE_CLAN_RAID_OMEGAMON_FINAL_BLOW',    'Kernel Core T1');

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

-- Remove da participação qualquer equipamento especial que tenha sido criado
-- por uma tentativa anterior. Os drops não-equipmento existentes são a base
-- real da contagem abaixo.
UPDATE loot_table_entries entry
SET active = FALSE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND entry.rarity = 'LEGENDARY'
  AND entry.item_type = 'EQUIPMENT'
  AND EXISTS (
      SELECT 1
      FROM special_boss_equipment_drops expected
      WHERE expected.table_code = table_row.code
  );

-- Conta as entradas lendárias ativas de cada tabela e divide a pool entre elas
-- e o novo equipamento. Como weight é inteiro, a primeira entrada recebe o
-- eventual resto do arredondamento para a soma permanecer exatamente 100.
WITH existing_entries AS (
    SELECT
        entry.id,
        table_row.code AS table_code,
        COUNT(*) OVER (PARTITION BY table_row.code) AS existing_count,
        ROW_NUMBER() OVER (PARTITION BY table_row.code ORDER BY entry.id) AS row_number
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    JOIN special_boss_equipment_drops expected ON expected.table_code = table_row.code
    WHERE entry.rarity = 'LEGENDARY'
      AND entry.item_type <> 'EQUIPMENT'
      AND entry.active = TRUE
), calculated_weights AS (
    SELECT
        id,
        row_number,
        existing_count,
        FLOOR(100.0 / (existing_count + 1))::INT AS base_weight
    FROM existing_entries
)
UPDATE loot_table_entries entry
SET weight = calculated_weights.base_weight
              + CASE
                    WHEN calculated_weights.row_number = 1
                    THEN 100 - calculated_weights.base_weight * calculated_weights.existing_count
                    ELSE 0
                END
FROM calculated_weights
WHERE entry.id = calculated_weights.id;

-- Insere uma única peça T1 por tabela com o mesmo peso-base calculado a partir
-- da contagem real dos drops existentes. Se não houver drop legado, recebe 100.
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
    FLOOR(100.0 / (COUNT(existing.id) + 1))::INT,
    1,
    1,
    TRUE
FROM special_boss_equipment_drops expected
JOIN loot_tables table_row ON table_row.code = expected.table_code
LEFT JOIN loot_table_entries existing
  ON existing.loot_table_id = table_row.id
 AND existing.rarity = 'LEGENDARY'
 AND existing.item_type <> 'EQUIPMENT'
 AND existing.active = TRUE
GROUP BY table_row.id, expected.equipment_template_name
HAVING NOT EXISTS (
    SELECT 1
    FROM loot_table_entries duplicate
    WHERE duplicate.loot_table_id = table_row.id
      AND duplicate.rarity = 'LEGENDARY'
      AND duplicate.item_type = 'EQUIPMENT'
      AND duplicate.equipment_template_name = expected.equipment_template_name
      AND duplicate.active = TRUE
);

DO $$
DECLARE
    invalid_pools INT;
    equipment_count INT;
BEGIN
    SELECT COUNT(*) INTO invalid_pools
    FROM (
        SELECT table_row.code
        FROM loot_table_entries entry
        JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND entry.rarity = 'LEGENDARY'
          AND EXISTS (
              SELECT 1
              FROM special_boss_equipment_drops expected
              WHERE expected.table_code = table_row.code
          )
        GROUP BY table_row.code
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    SELECT COUNT(*) INTO equipment_count
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    JOIN special_boss_equipment_drops expected
      ON expected.table_code = table_row.code
     AND expected.equipment_template_name = entry.equipment_template_name
    WHERE entry.active = TRUE
      AND entry.rarity = 'LEGENDARY'
      AND entry.item_type = 'EQUIPMENT'
      AND entry.min_quantity = 1
      AND entry.max_quantity = 1;

    IF invalid_pools > 0 OR equipment_count <> 6 THEN
        RAISE EXCEPTION
            'Recompensas especiais: % pool(s) lendária(s) não somam 100; equipamentos T1 válidos=%',
            invalid_pools, equipment_count;
    END IF;
END $$;

COMMIT;
