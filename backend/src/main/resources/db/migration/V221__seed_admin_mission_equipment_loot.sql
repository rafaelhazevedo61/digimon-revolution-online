-- Cenário de validação estrutural: a missão administrativa permanece fora do fluxo
-- normal do jogador e recebe uma entrada de raridade fixa e outra aleatória.
UPDATE mission_definitions
SET active = FALSE
WHERE id = 'MISSION_ADMIN';

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
    'EPIC',
    'EQUIPMENT',
    NULL,
    'Garra Berserker T1',
    'RARE',
    1,
    1,
    1,
    TRUE
FROM loot_tables lt
WHERE lt.code = 'LOOT_TABLE_MISSION_MISSION_ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM loot_table_entries entry
      WHERE entry.loot_table_id = lt.id
        AND entry.item_type = 'EQUIPMENT'
        AND entry.equipment_template_name = 'Garra Berserker T1'
  );

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
    'Couraça Berserker T1',
    NULL,
    1,
    1,
    1,
    TRUE
FROM loot_tables lt
WHERE lt.code = 'LOOT_TABLE_MISSION_MISSION_ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM loot_table_entries entry
      WHERE entry.loot_table_id = lt.id
        AND entry.item_type = 'EQUIPMENT'
        AND entry.equipment_template_name = 'Couraça Berserker T1'
  );
