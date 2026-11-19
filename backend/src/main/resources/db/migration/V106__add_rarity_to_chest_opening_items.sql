ALTER TABLE chest_opening_items
    ADD COLUMN rarity VARCHAR(20);

UPDATE chest_opening_items coi
SET rarity = co.rarity
FROM chest_openings co
WHERE co.id = coi.chest_opening_id
  AND coi.rarity IS NULL;

ALTER TABLE chest_opening_items
    ALTER COLUMN rarity SET NOT NULL;

ALTER TABLE chest_opening_items
    ADD CONSTRAINT ck_chest_opening_item_rarity_known
        CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'));
