BEGIN;

-- Digitamas e incubadoras são selecionados exclusivamente no fluxo de incubação.
UPDATE item_definitions
SET usable = false
WHERE category IN ('DIGITAMA', 'INCUBATOR');

-- O inventário representa uma quantidade lógica por item catalogado.
-- O limite anterior de 10 permitia que linhas históricas fossem exibidas como
-- duplicadas sem que o fluxo normal de concessão conseguisse consolidá-las.
UPDATE item_definitions
SET max_stack = 99
WHERE category IN ('DIGITAMA', 'INCUBATOR')
  AND (max_stack IS NULL OR max_stack < 99);

-- Consolida linhas antigas e linhas catalogadas do mesmo Digimon/item.
-- A seleção do sobrevivente é determinística e a soma ocorre antes da remoção
-- das demais linhas para preservar toda a quantidade existente.
CREATE TEMP TABLE inventory_item_consolidation ON COMMIT DROP AS
SELECT
    inventory.digimon_id,
    MIN(inventory.id) AS survivor_id,
    definition.id AS item_definition_id,
    definition.code AS item_code,
    SUM(inventory.quantity) AS total_quantity
FROM inventory_items inventory
JOIN item_definitions definition
    ON definition.code = inventory.item_type
WHERE definition.code IN (
    'POTION_SMALL',
    'TRAINING_STONE',
    'DATA_CORE',
    'DIGITAMA_STARTER',
    'DIGITAMA_FIRE',
    'DIGITAMA_WATER',
    'DIGITAMA_NATURE',
    'INCUBATOR_COMMON',
    'INCUBATOR_RARE',
    'INCUBATOR_EPIC'
)
GROUP BY inventory.digimon_id, definition.id, definition.code
HAVING COUNT(*) > 1
    OR BOOL_OR(inventory.item_definition_id IS NULL);

DELETE FROM inventory_items duplicate
USING inventory_item_consolidation consolidation
WHERE duplicate.digimon_id = consolidation.digimon_id
  AND duplicate.id <> consolidation.survivor_id
  AND (
      duplicate.item_definition_id = consolidation.item_definition_id
      OR (
          duplicate.item_definition_id IS NULL
          AND duplicate.item_type = consolidation.item_code
      )
  );

UPDATE inventory_items survivor
SET item_definition_id = consolidation.item_definition_id,
    item_type = consolidation.item_code,
    quantity = consolidation.total_quantity
FROM inventory_item_consolidation consolidation
WHERE survivor.id = consolidation.survivor_id;

COMMIT;
