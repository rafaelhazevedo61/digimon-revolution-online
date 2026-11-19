-- Normalizar raridade dos templates para COMMON
-- Raridade agora e uma propriedade da instancia do equipamento, nao do template.
-- O template define apenas: set, slot, tier e stats base.
-- A raridade e determinada no momento do grant/drop (shop, boss, admin).
-- O multiplicador de raridade e aplicado em runtime via Equipment.getEffectiveBonusX().

UPDATE equipment_templates SET rarity = 'COMMON';

-- Tornar rarity nullable no template (o template nao precisa definir raridade)
ALTER TABLE equipment_templates ALTER COLUMN rarity DROP NOT NULL;
