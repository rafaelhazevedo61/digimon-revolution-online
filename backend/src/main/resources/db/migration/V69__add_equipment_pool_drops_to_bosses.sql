-- Remove drops de EQUIPMENT_POOL (se existirem de migration anterior)
DELETE FROM boss_drops WHERE drop_type = 'EQUIPMENT_POOL';

-- Remove drops de EQUIPMENT antigos (se existirem)
DELETE FROM boss_drops WHERE drop_type = 'EQUIPMENT';

-- Adiciona todos os 120 equipment templates como drops EQUIPMENT para cada boss.
-- Chance = 25% (usada como chance do pool: 1 roll, se passar sorteia 1 template aleatorio).
-- Raridade e rolada em runtime via EquipmentRarityRules usando o perfil do boss type.
INSERT INTO boss_drops (boss_id, drop_type, template_name, chance, min_quantity, max_quantity)
SELECT bd.id, 'EQUIPMENT', et.name, 25, 1, 1
FROM boss_definitions bd
CROSS JOIN equipment_templates et
WHERE et.active = true;
