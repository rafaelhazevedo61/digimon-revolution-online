-- Consolida linhas legadas sem item_definition_id quando já existe uma linha
-- catalogada equivalente para o mesmo Digimon e código de item.
-- Materiais nomeados permanecem separados porque seu item_type é
-- EVOLUTION_MATERIAL e seu código não é igual ao enum do tipo.
CREATE TEMP TABLE inventory_catalog_merge ON COMMIT DROP AS
SELECT
    cataloged.id AS cataloged_id,
    ARRAY_AGG(legacy.id) AS legacy_ids,
    SUM(legacy.quantity) AS legacy_quantity
FROM inventory_items legacy
JOIN item_definitions definition
    ON definition.code = legacy.item_type
   AND definition.category <> 'CHEST'
JOIN inventory_items cataloged
    ON cataloged.digimon_id = legacy.digimon_id
   AND cataloged.item_definition_id = definition.id
WHERE legacy.item_definition_id IS NULL
GROUP BY cataloged.id, cataloged.quantity, definition.max_stack
HAVING definition.max_stack IS NULL
    OR cataloged.quantity + SUM(legacy.quantity) <= definition.max_stack;

UPDATE inventory_items cataloged
SET quantity = cataloged.quantity + merge.legacy_quantity
FROM inventory_catalog_merge merge
WHERE cataloged.id = merge.cataloged_id;

DELETE FROM inventory_items legacy
USING inventory_catalog_merge merge
WHERE legacy.id = ANY(merge.legacy_ids);
