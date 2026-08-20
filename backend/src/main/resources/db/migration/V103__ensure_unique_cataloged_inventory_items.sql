CREATE UNIQUE INDEX IF NOT EXISTS ux_inventory_items_digimon_item_definition
    ON inventory_items (digimon_id, item_definition_id)
    WHERE item_definition_id IS NOT NULL;
