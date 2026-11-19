-- Ajusta a última missão da Montanha Infinita (Desafio do Soberano).
-- A raridade LEGENDARY passa a ter 25% de chance e a pool lendária
-- passa a distribuir igualmente os quatro drops configurados.
BEGIN;

-- Mantém a soma das chances de raridade em 100%.
UPDATE loot_table_rarity_weights rarity_weight
SET weight = CASE rarity_weight.rarity
    WHEN 'COMMON' THEN 30
    WHEN 'LEGENDARY' THEN 25
    ELSE rarity_weight.weight
END
FROM loot_tables table_row
WHERE rarity_weight.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
  AND rarity_weight.rarity IN ('COMMON', 'LEGENDARY');

-- Adiciona o Dado de Raridade à pool lendária caso ainda não exista.
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
    'RARITY_REROLL',
    NULL,
    NULL,
    NULL,
    25,
    1,
    1,
    TRUE
FROM loot_tables table_row
WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
  AND NOT EXISTS (
      SELECT 1
      FROM loot_table_entries entry
      WHERE entry.loot_table_id = table_row.id
        AND entry.rarity = 'LEGENDARY'
        AND entry.item_type = 'RARITY_REROLL'
  );

-- XP, Núcleo de Dados, equipamento e Dado de Raridade ficam com 25% cada
-- dentro da pool LEGENDARY.
UPDATE loot_table_entries entry
SET weight = 25,
    active = TRUE
FROM loot_tables table_row
WHERE entry.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
  AND entry.rarity = 'LEGENDARY'
  AND entry.item_type IN ('XP_DISC_1', 'DATA_CORE', 'EQUIPMENT', 'RARITY_REROLL');

DO $$
DECLARE
    legendary_rarity_weight INT;
    rarity_weight_total INT;
    legendary_entry_count INT;
    invalid_legendary_entries INT;
    legendary_entry_weight_total INT;
    rarity_reroll_count INT;
BEGIN
    SELECT COALESCE(SUM(weight), 0)
    INTO rarity_weight_total
    FROM loot_table_rarity_weights rarity_weight
    JOIN loot_tables table_row ON table_row.id = rarity_weight.loot_table_id
    WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3';

    SELECT weight
    INTO legendary_rarity_weight
    FROM loot_table_rarity_weights rarity_weight
    JOIN loot_tables table_row ON table_row.id = rarity_weight.loot_table_id
    WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
      AND rarity_weight.rarity = 'LEGENDARY';

    SELECT COUNT(*), COUNT(*) FILTER (WHERE entry.weight <> 25), COALESCE(SUM(entry.weight), 0)
    INTO legendary_entry_count, invalid_legendary_entries, legendary_entry_weight_total
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
      AND entry.rarity = 'LEGENDARY'
      AND entry.active = TRUE;

    SELECT COUNT(*)
    INTO rarity_reroll_count
    FROM loot_table_entries entry
    JOIN loot_tables table_row ON table_row.id = entry.loot_table_id
    WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
      AND entry.rarity = 'LEGENDARY'
      AND entry.item_type = 'RARITY_REROLL'
      AND entry.active = TRUE
      AND entry.weight = 25;

    IF rarity_weight_total <> 100 OR legendary_rarity_weight <> 25 THEN
        RAISE EXCEPTION 'Infinity Mountain IM_3: pesos de raridade inválidos (total=%, legendary=%).',
            rarity_weight_total, legendary_rarity_weight;
    END IF;

    IF legendary_entry_count <> 4
       OR invalid_legendary_entries <> 0
       OR legendary_entry_weight_total <> 100
       OR rarity_reroll_count <> 1 THEN
        RAISE EXCEPTION 'Infinity Mountain IM_3: pool lendária inválida (entradas=%, pesos inválidos=%, total=%, dados=%).',
            legendary_entry_count, invalid_legendary_entries, legendary_entry_weight_total, rarity_reroll_count;
    END IF;
END $$;

COMMIT;
