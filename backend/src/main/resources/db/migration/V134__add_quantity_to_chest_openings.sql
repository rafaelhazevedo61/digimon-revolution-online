ALTER TABLE chest_openings
    ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1;

ALTER TABLE chest_openings
    DROP CONSTRAINT IF EXISTS ck_chest_opening_quantity_positive;

ALTER TABLE chest_openings
    ADD CONSTRAINT ck_chest_opening_quantity_positive CHECK (quantity > 0);
