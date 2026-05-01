-- Limpa dados antigos vinculados a player_id
TRUNCATE TABLE inventory_items;

-- Remove coluna player_id e adiciona coluna digimon_id
ALTER TABLE inventory_items DROP COLUMN player_id;
ALTER TABLE inventory_items ADD COLUMN digimon_id UUID NOT NULL;
