-- Migra a posse de itens e equipamentos do Digimon para o jogador.
-- O excedente acima de item_definitions.max_stack é descartado conforme a regra de negócio.

ALTER TABLE inventory_items ADD COLUMN player_id UUID;

UPDATE inventory_items inventory
SET player_id = digimon.player_id
FROM digimons digimon
WHERE inventory.digimon_id = digimon.id
  AND inventory.player_id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM inventory_items WHERE player_id IS NULL) THEN
        RAISE EXCEPTION 'inventory_items possui registros sem jogador proprietário';
    END IF;
END $$;

CREATE TEMP TABLE inventory_catalogued_consolidation ON COMMIT DROP AS
SELECT
    player_id,
    item_definition_id,
    MIN(id::text)::uuid AS survivor_id,
    LEAST(SUM(quantity), COALESCE(MAX(definition.max_stack), 2147483647))::int AS consolidated_quantity
FROM inventory_items inventory
JOIN item_definitions definition ON definition.id = inventory.item_definition_id
WHERE inventory.item_definition_id IS NOT NULL
GROUP BY player_id, item_definition_id;

DELETE FROM inventory_items duplicate
USING inventory_catalogued_consolidation consolidation
WHERE duplicate.player_id = consolidation.player_id
  AND duplicate.item_definition_id = consolidation.item_definition_id
  AND duplicate.id <> consolidation.survivor_id;

UPDATE inventory_items inventory
SET quantity = consolidation.consolidated_quantity
FROM inventory_catalogued_consolidation consolidation
WHERE inventory.id = consolidation.survivor_id;

CREATE TEMP TABLE inventory_legacy_consolidation ON COMMIT DROP AS
SELECT
    player_id,
    item_type,
    MIN(id::text)::uuid AS survivor_id,
    SUM(quantity)::int AS consolidated_quantity
FROM inventory_items
WHERE item_definition_id IS NULL
GROUP BY player_id, item_type;

DELETE FROM inventory_items duplicate
USING inventory_legacy_consolidation consolidation
WHERE duplicate.player_id = consolidation.player_id
  AND duplicate.item_type = consolidation.item_type
  AND duplicate.item_definition_id IS NULL
  AND duplicate.id <> consolidation.survivor_id;

UPDATE inventory_items inventory
SET quantity = consolidation.consolidated_quantity
FROM inventory_legacy_consolidation consolidation
WHERE inventory.id = consolidation.survivor_id;

DROP INDEX IF EXISTS ux_inventory_items_digimon_item_definition;
DROP INDEX IF EXISTS idx_inventory_items_digimon_id;
ALTER TABLE inventory_items DROP COLUMN digimon_id;
ALTER TABLE inventory_items ALTER COLUMN player_id SET NOT NULL;
ALTER TABLE inventory_items ADD CONSTRAINT fk_inventory_items_player FOREIGN KEY (player_id) REFERENCES players(id);
CREATE UNIQUE INDEX ux_inventory_items_player_item_definition
    ON inventory_items (player_id, item_definition_id)
    WHERE item_definition_id IS NOT NULL;
CREATE INDEX idx_inventory_items_player_id ON inventory_items(player_id);

ALTER TABLE equipments ADD COLUMN player_id UUID;

UPDATE equipments equipment
SET player_id = digimon.player_id
FROM digimons digimon
WHERE equipment.digimon_id = digimon.id
  AND equipment.player_id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM equipments WHERE player_id IS NULL) THEN
        RAISE EXCEPTION 'equipments possui registros sem jogador proprietário';
    END IF;
END $$;

UPDATE equipments
SET digimon_id = NULL
WHERE equipped = FALSE;

DROP INDEX IF EXISTS idx_equipments_digimon_id;
ALTER TABLE equipments ALTER COLUMN player_id SET NOT NULL;
ALTER TABLE equipments ADD CONSTRAINT fk_equipments_player FOREIGN KEY (player_id) REFERENCES players(id);
ALTER TABLE equipments ALTER COLUMN digimon_id DROP NOT NULL;
CREATE INDEX idx_equipments_player_id ON equipments(player_id);
CREATE INDEX idx_equipments_equipped_digimon ON equipments(digimon_id) WHERE equipped = TRUE;
