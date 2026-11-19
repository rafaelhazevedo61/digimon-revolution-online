-- Limpar equipment_rarity hardcoded dos boss drops.
-- Agora a raridade e rolada automaticamente via EquipmentRarityRules.rollRarity()
-- quando nenhuma raridade e especificada no drop.
-- Chances: 60% Common, 25% Rare, 12% Epic, 3% Legendary.

UPDATE boss_drops SET equipment_rarity = NULL WHERE drop_type = 'EQUIPMENT';
