ALTER TABLE loot_table_entries
    ADD COLUMN equipment_template_name VARCHAR(120),
    ADD COLUMN equipment_rarity VARCHAR(20),
    ADD CONSTRAINT ck_loot_entry_equipment_rarity CHECK (
        equipment_rarity IS NULL OR equipment_rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY')
    ),
    ADD CONSTRAINT ck_loot_entry_equipment_fields CHECK (
        (item_type = 'EQUIPMENT' AND equipment_template_name IS NOT NULL AND btrim(equipment_template_name) <> '' AND material_code IS NULL AND min_quantity = 1 AND max_quantity = 1)
        OR (item_type <> 'EQUIPMENT' AND equipment_template_name IS NULL AND equipment_rarity IS NULL)
    );

ALTER TABLE chest_opening_items
    ADD COLUMN equipment_template_name VARCHAR(120),
    ADD COLUMN equipment_rarity VARCHAR(20),
    ADD CONSTRAINT ck_chest_opening_equipment_rarity CHECK (
        equipment_rarity IS NULL OR equipment_rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY')
    ),
    ADD CONSTRAINT ck_chest_opening_equipment_fields CHECK (
        (item_type = 'EQUIPMENT' AND equipment_template_name IS NOT NULL AND equipment_rarity IS NOT NULL)
        OR (item_type <> 'EQUIPMENT' AND equipment_template_name IS NULL AND equipment_rarity IS NULL)
    );
