ALTER TABLE inventory_equipments
    ADD COLUMN IF NOT EXISTS ascension_level INTEGER NOT NULL DEFAULT 0;

ALTER TABLE inventory_equipments
    ADD CONSTRAINT ck_inventory_equipments_ascension_level
    CHECK (ascension_level >= 0 AND ascension_level <= 3);
