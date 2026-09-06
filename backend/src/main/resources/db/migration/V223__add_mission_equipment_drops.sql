BEGIN;

-- V223: adiciona os equipamentos T1 mapeados na planilha consolidada às 18 missões.
-- A coluna rarity abaixo representa a raridade da pool da Loot Table (LEGENDARY).
-- equipment_rarity permanece NULL para sortear a raridade efetiva do equipamento
-- na abertura, conforme o fluxo oficial de GrantEquipmentUseCase.
CREATE TEMP TABLE mission_equipment_drops (
    table_code VARCHAR(80) NOT NULL,
    equipment_template_name VARCHAR(120) NOT NULL,
    weight INT NOT NULL
) ON COMMIT DROP;

INSERT INTO mission_equipment_drops (table_code, equipment_template_name, weight)
VALUES
    ('LOOT_TABLE_MISSION_MISSION_1',    'Garra Berserker T1',       50),
    ('LOOT_TABLE_MISSION_MISSION_NF_2', 'Couraça Berserker T1',     50),
    ('LOOT_TABLE_MISSION_MISSION_NF_3', 'Emblema Berserker T1',     50),
    ('LOOT_TABLE_MISSION_MISSION_2',    'Lança Guardiã T1',         50),
    ('LOOT_TABLE_MISSION_MISSION_GS_2', 'Armadura Guardiã T1',      50),
    ('LOOT_TABLE_MISSION_MISSION_GS_3', 'Medalha Guardiã T1',       50),
    ('LOOT_TABLE_MISSION_MISSION_3',    'Cetro da Vitalidade T1',   50),
    ('LOOT_TABLE_MISSION_MISSION_FT_2', 'Vestes da Vitalidade T1',  50),
    ('LOOT_TABLE_MISSION_MISSION_FT_3', 'Emblema da Vitalidade T1', 50),
    ('LOOT_TABLE_MISSION_MISSION_4',    'Lâmina do Equilíbrio T1',  50),
    ('LOOT_TABLE_MISSION_MISSION_FL_2', 'Armadura do Equilíbrio T1',50),
    ('LOOT_TABLE_MISSION_MISSION_FL_3', 'Símbolo do Equilíbrio T1', 50),
    ('LOOT_TABLE_MISSION_MISSION_5',    'Lâmina Overclock T1',      50),
    ('LOOT_TABLE_MISSION_MISSION_SD_2', 'Frame Overclock T1',       50),
    ('LOOT_TABLE_MISSION_MISSION_SD_3', 'Chip Overclock T1',         50),
    ('LOOT_TABLE_MISSION_MISSION_6',    'Lança Firewall T1',        33),
    ('LOOT_TABLE_MISSION_MISSION_IM_2', 'Couraça Firewall T1',      33),
    ('LOOT_TABLE_MISSION_MISSION_IM_3', 'Núcleo Firewall T1',       33);

DO $$
DECLARE
    missing_tables INT;
    missing_templates INT;
BEGIN
    SELECT COUNT(*) INTO missing_tables
    FROM mission_equipment_drops expected
    LEFT JOIN loot_tables table_row ON table_row.code = expected.table_code
    WHERE table_row.id IS NULL;

    IF missing_tables > 0 THEN
        RAISE EXCEPTION 'Missões: % Loot Table(s) não encontrada(s) para os equipamentos T1.', missing_tables;
    END IF;

    SELECT COUNT(*) INTO missing_templates
    FROM mission_equipment_drops expected
    LEFT JOIN equipment_templates template
      ON template.name = expected.equipment_template_name
     AND template.active = TRUE
    WHERE template.name IS NULL;

    IF missing_templates > 0 THEN
        RAISE EXCEPTION 'Missões: % template(s) de equipamento T1 não encontrado(s) ou inativo(s).', missing_templates;
    END IF;
END $$;

-- As missões 1–5 de cada área têm dois drops lendários: XP e equipamento.
-- A missão 6 e suas continuações têm três: XP, Núcleo de Dados e equipamento.
UPDATE loot_table_entries entry
SET weight = CASE
    WHEN entry.item_type = 'XP_DISC_1'
         AND lt.code IN (
             'LOOT_TABLE_MISSION_MISSION_6',
             'LOOT_TABLE_MISSION_MISSION_IM_2',
             'LOOT_TABLE_MISSION_MISSION_IM_3'
         ) THEN 34
    WHEN entry.item_type = 'DATA_CORE'
         AND lt.code IN (
             'LOOT_TABLE_MISSION_MISSION_6',
             'LOOT_TABLE_MISSION_MISSION_IM_2',
             'LOOT_TABLE_MISSION_MISSION_IM_3'
         ) THEN 33
    WHEN entry.item_type = 'XP_DISC_1' THEN 50
    ELSE entry.weight
END
FROM loot_tables lt
WHERE entry.loot_table_id = lt.id
  AND entry.rarity = 'LEGENDARY'
  AND entry.item_type IN ('XP_DISC_1', 'DATA_CORE')
  AND EXISTS (
      SELECT 1
      FROM mission_equipment_drops expected
      WHERE expected.table_code = lt.code
  );

-- Reaplica os registros caso uma migration tenha sido parcialmente executada
-- ou caso um ambiente já contenha uma entrada equivalente.
UPDATE loot_table_entries entry
SET rarity = 'LEGENDARY',
    item_type = 'EQUIPMENT',
    material_code = NULL,
    equipment_template_name = expected.equipment_template_name,
    equipment_rarity = NULL,
    weight = expected.weight,
    min_quantity = 1,
    max_quantity = 1,
    active = TRUE
FROM loot_tables lt
JOIN mission_equipment_drops expected ON expected.table_code = lt.code
WHERE entry.loot_table_id = lt.id
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
    lt.id,
    'LEGENDARY',
    'EQUIPMENT',
    NULL,
    expected.equipment_template_name,
    NULL,
    expected.weight,
    1,
    1,
    TRUE
FROM mission_equipment_drops expected
JOIN loot_tables lt ON lt.code = expected.table_code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = lt.id
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
    JOIN loot_tables lt ON lt.id = entry.loot_table_id
    WHERE entry.active = TRUE
      AND entry.item_type = 'EQUIPMENT'
      AND entry.rarity = 'LEGENDARY'
      AND lt.code IN (SELECT table_code FROM mission_equipment_drops);

    IF equipment_count <> 18 THEN
        RAISE EXCEPTION 'Missões: esperados 18 drops de equipamento T1 ativos, encontrados %.', equipment_count;
    END IF;

    SELECT COUNT(*) INTO invalid_pools
    FROM (
        SELECT lt.code
        FROM loot_table_entries entry
        JOIN loot_tables lt ON lt.id = entry.loot_table_id
        WHERE entry.active = TRUE
          AND lt.code IN (SELECT table_code FROM mission_equipment_drops)
          AND entry.rarity = 'LEGENDARY'
        GROUP BY lt.code
        HAVING SUM(entry.weight) <> 100
    ) invalid;

    IF invalid_pools > 0 THEN
        RAISE EXCEPTION 'Missões: % pool(s) lendária(s) não somam 100 após adicionar equipamentos.', invalid_pools;
    END IF;
END $$;

COMMIT;
