ALTER TABLE inventory_items ADD COLUMN item_definition_id BIGINT REFERENCES item_definitions(id);

ALTER TABLE inventory_items DROP COLUMN IF EXISTS material_code;
