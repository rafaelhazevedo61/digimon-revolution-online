-- Remove digimon_id from equipments and add equipped boolean
ALTER TABLE equipments DROP COLUMN IF EXISTS digimon_id;
DROP INDEX IF EXISTS idx_equipments_digimon_id;

ALTER TABLE equipments ADD COLUMN equipped BOOLEAN NOT NULL DEFAULT FALSE;

-- Add equipment slot columns to digimons table
ALTER TABLE digimons ADD COLUMN weapon_id UUID;
ALTER TABLE digimons ADD COLUMN armor_id UUID;
ALTER TABLE digimons ADD COLUMN accessory_id UUID;
