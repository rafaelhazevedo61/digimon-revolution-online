-- Corrige os pesos externos de raridade da última missão da Montanha Infinita.
-- A composição correta é COMMON 50%, RARE 30%, EPIC 15% e LEGENDARY 5%.
-- Os pesos internos da pool LEGENDARY permanecem definidos na V237.
BEGIN;

UPDATE loot_table_rarity_weights rarity_weight
SET weight = CASE rarity_weight.rarity
    WHEN 'COMMON' THEN 50
    WHEN 'LEGENDARY' THEN 5
    ELSE rarity_weight.weight
END
FROM loot_tables table_row
WHERE rarity_weight.loot_table_id = table_row.id
  AND table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3'
  AND rarity_weight.rarity IN ('COMMON', 'LEGENDARY');

DO $$
DECLARE
    rarity_weight_total INT;
    common_weight INT;
    rare_weight INT;
    epic_weight INT;
    legendary_weight INT;
BEGIN
    SELECT COALESCE(SUM(weight), 0)
    INTO rarity_weight_total
    FROM loot_table_rarity_weights rarity_weight
    JOIN loot_tables table_row ON table_row.id = rarity_weight.loot_table_id
    WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3';

    SELECT MAX(weight) FILTER (WHERE rarity_weight.rarity = 'COMMON'),
           MAX(weight) FILTER (WHERE rarity_weight.rarity = 'RARE'),
           MAX(weight) FILTER (WHERE rarity_weight.rarity = 'EPIC'),
           MAX(weight) FILTER (WHERE rarity_weight.rarity = 'LEGENDARY')
    INTO common_weight, rare_weight, epic_weight, legendary_weight
    FROM loot_table_rarity_weights rarity_weight
    JOIN loot_tables table_row ON table_row.id = rarity_weight.loot_table_id
    WHERE table_row.code = 'LOOT_TABLE_MISSION_MISSION_IM_3';

    IF rarity_weight_total <> 100
       OR common_weight <> 50
       OR rare_weight <> 30
       OR epic_weight <> 15
       OR legendary_weight <> 5 THEN
        RAISE EXCEPTION 'Infinity Mountain IM_3: pesos de raridade inválidos (common=%, rare=%, epic=%, legendary=%, total=%).',
            common_weight, rare_weight, epic_weight, legendary_weight, rarity_weight_total;
    END IF;
END $$;

COMMIT;
