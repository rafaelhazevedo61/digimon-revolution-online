-- Migra a posse de itens e equipamentos do Digimon para o jogador.
-- O excedente acima de item_definitions.max_stack é descartado conforme a regra de negócio.
--
-- A migration não atribui órfãos a um jogador arbitrário. Ela tenta recuperar o dono
-- por relações históricas não destrutivas e aborta com os IDs pendentes quando não
-- houver evidência suficiente.

ALTER TABLE inventory_items ADD COLUMN player_id UUID;

-- Caso normal: o Digimon ainda existe.
UPDATE inventory_items inventory
SET player_id = digimon.player_id
FROM digimons digimon
WHERE inventory.digimon_id = digimon.id
  AND inventory.player_id IS NULL;

-- Recupera itens cujo Digimon original foi substituído por rebirth. Só aceita a
-- relação quando ela aponta para exatamente um jogador distinto.
WITH rebirth_candidates AS (
    SELECT inventory.id AS inventory_item_id,
           (ARRAY_AGG(current_digimon.player_id ORDER BY current_digimon.player_id))[1] AS player_id
    FROM inventory_items inventory
    JOIN digimons current_digimon
      ON current_digimon.reborned_from = inventory.digimon_id
    WHERE inventory.player_id IS NULL
      AND NOT EXISTS (
          SELECT 1
          FROM digimons original_digimon
          WHERE original_digimon.id = inventory.digimon_id
      )
    GROUP BY inventory.id
    HAVING COUNT(DISTINCT current_digimon.player_id) = 1
)
UPDATE inventory_items inventory
SET player_id = candidates.player_id
FROM rebirth_candidates candidates
WHERE inventory.id = candidates.inventory_item_id
  AND inventory.player_id IS NULL;

-- Recupera casos em que o histórico de missões é a única relação remanescente.
-- Também exige exatamente um jogador distinto para evitar transferências indevidas.
WITH mission_candidates AS (
    SELECT inventory.id AS inventory_item_id,
           (ARRAY_AGG(mission.player_id ORDER BY mission.player_id))[1] AS player_id
    FROM inventory_items inventory
    JOIN mission_instances mission
      ON mission.digimon_id = inventory.digimon_id
    WHERE inventory.player_id IS NULL
      AND NOT EXISTS (
          SELECT 1
          FROM digimons original_digimon
          WHERE original_digimon.id = inventory.digimon_id
      )
    GROUP BY inventory.id
    HAVING COUNT(DISTINCT mission.player_id) = 1
)
UPDATE inventory_items inventory
SET player_id = candidates.player_id
FROM mission_candidates candidates
WHERE inventory.id = candidates.inventory_item_id
  AND inventory.player_id IS NULL;

DO $$
DECLARE
    unresolved_count INTEGER;
    unresolved_ids TEXT;
BEGIN
    SELECT COUNT(*), string_agg(inventory.id::text, ', ' ORDER BY inventory.id)
    INTO unresolved_count, unresolved_ids
    FROM inventory_items inventory
    WHERE inventory.player_id IS NULL;

    IF unresolved_count > 0 THEN
        RAISE EXCEPTION
            'inventory_items possui % registros sem jogador proprietário. IDs: %. '
            'Recupere o vínculo ou cadastre um mapeamento confiável antes de repetir a migration.',
            unresolved_count,
            unresolved_ids;
    END IF;
END $$;

CREATE TEMP TABLE inventory_catalogued_consolidation ON COMMIT DROP AS
SELECT
    inventory.player_id,
    inventory.item_definition_id,
    MIN(inventory.id::text)::uuid AS survivor_id,
    LEAST(SUM(inventory.quantity), COALESCE(MAX(definition.max_stack), 2147483647))::int AS consolidated_quantity
FROM inventory_items inventory
JOIN item_definitions definition ON definition.id = inventory.item_definition_id
WHERE inventory.item_definition_id IS NOT NULL
GROUP BY inventory.player_id, inventory.item_definition_id;

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
    inventory.player_id,
    inventory.item_type,
    MIN(inventory.id::text)::uuid AS survivor_id,
    SUM(inventory.quantity)::int AS consolidated_quantity
FROM inventory_items inventory
WHERE inventory.item_definition_id IS NULL
GROUP BY inventory.player_id, inventory.item_type;

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
DECLARE
    unresolved_count INTEGER;
    unresolved_ids TEXT;
BEGIN
    SELECT COUNT(*), string_agg(equipment.id::text, ', ' ORDER BY equipment.id)
    INTO unresolved_count, unresolved_ids
    FROM equipments equipment
    WHERE equipment.player_id IS NULL;

    IF unresolved_count > 0 THEN
        RAISE EXCEPTION
            'equipments possui % registros sem jogador proprietário. IDs: %',
            unresolved_count,
            unresolved_ids;
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
