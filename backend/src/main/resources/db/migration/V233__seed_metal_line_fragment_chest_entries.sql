-- Adiciona os fragmentos das linhas MetalKoromon/Mekamon (V163) às pools de baú por estágio.
-- Nenhum produto de loja é criado ou alterado.
INSERT INTO loot_table_entries (loot_table_id, rarity, item_type, material_code, weight, min_quantity, max_quantity, active)
SELECT table_row.id, data.rarity, 'EVOLUTION_MATERIAL', data.material_code, 1, data.min_quantity, data.max_quantity, data.active
FROM (
    VALUES
    ('LOOT_TABLE_SHOP_FRAGMENT_ROOKIE',   'COMMON',    'FRAGMENT_HAGURUMON',      1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_CHAMPION', 'RARE',      'FRAGMENT_THUNDERBALLMON', 1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_ULTIMATE', 'EPIC',      'FRAGMENT_MEGADRAMON',     1, 5, TRUE),
    ('LOOT_TABLE_SHOP_FRAGMENT_MEGA',     'LEGENDARY', 'FRAGMENT_MACHINEDRAMON',  1, 5, TRUE)
) AS data(table_code, rarity, material_code, min_quantity, max_quantity, active)
JOIN loot_tables table_row ON table_row.code = data.table_code
WHERE NOT EXISTS (
    SELECT 1 FROM loot_table_entries existing
     WHERE existing.loot_table_id = table_row.id
       AND existing.rarity = data.rarity
       AND existing.item_type = 'EVOLUTION_MATERIAL'
       AND existing.material_code = data.material_code
);
