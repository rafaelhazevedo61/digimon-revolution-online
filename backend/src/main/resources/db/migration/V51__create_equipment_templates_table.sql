-- slot:   WEAPON, ARMOR, ACCESSORY
-- rarity: COMMON, RARE, EPIC, LEGENDARY

CREATE TABLE equipment_templates (
    name          VARCHAR(120) PRIMARY KEY,
    slot          VARCHAR(20) NOT NULL,     -- WEAPON | ARMOR | ACCESSORY
    rarity        VARCHAR(20) NOT NULL,     -- COMMON | RARE | EPIC | LEGENDARY
    bonus_hp      INT NOT NULL DEFAULT 0,
    bonus_attack  INT NOT NULL DEFAULT 0,
    bonus_defense INT NOT NULL DEFAULT 0,
    active        BOOLEAN NOT NULL DEFAULT TRUE
);
