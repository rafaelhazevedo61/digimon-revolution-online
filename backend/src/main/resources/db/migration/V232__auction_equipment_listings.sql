-- Existing listings remain ITEM. Equipment details are immutable historical snapshots.
ALTER TABLE inventory_equipments
    ADD COLUMN availability VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    ADD CONSTRAINT chk_equipment_availability CHECK (availability IN ('AVAILABLE', 'AUCTION_ESCROW')),
    ADD CONSTRAINT chk_equipment_escrow CHECK (
        availability <> 'AUCTION_ESCROW' OR (equipped = FALSE AND locked = FALSE AND digimon_id IS NULL));

ALTER TABLE auction_listings
    ALTER COLUMN item_definition_id DROP NOT NULL,
    ADD COLUMN listing_type VARCHAR(20) NOT NULL DEFAULT 'ITEM',
    -- No FK: later refinement/destruction must not invalidate the sale history.
    ADD COLUMN equipment_id UUID,
    ADD COLUMN equipment_name VARCHAR(255),
    ADD COLUMN equipment_slot VARCHAR(255),
    ADD COLUMN equipment_rarity VARCHAR(255),
    ADD COLUMN equipment_set_code VARCHAR(255),
    ADD COLUMN equipment_tier INT,
    ADD COLUMN equipment_refinement_level INT,
    ADD COLUMN equipment_ascension_level INT,
    ADD COLUMN equipment_bonus_hp INT,
    ADD COLUMN equipment_bonus_attack INT,
    ADD COLUMN equipment_bonus_defense INT,
    ADD COLUMN equipment_effective_bonus_hp INT,
    ADD COLUMN equipment_effective_bonus_attack INT,
    ADD COLUMN equipment_effective_bonus_defense INT,
    ADD CONSTRAINT chk_auction_asset CHECK (
        (listing_type = 'ITEM' AND item_definition_id IS NOT NULL AND equipment_id IS NULL)
        OR (listing_type = 'EQUIPMENT' AND item_definition_id IS NULL AND equipment_id IS NOT NULL
            AND equipment_name IS NOT NULL AND equipment_slot IS NOT NULL AND equipment_rarity IS NOT NULL
            AND quantity = 1 AND remaining_quantity IN (0, 1)));

-- Keep expired-but-not-returned listings reserved until settlement completes.
CREATE UNIQUE INDEX ux_auction_active_equipment ON auction_listings(equipment_id)
    WHERE listing_type = 'EQUIPMENT' AND status = 'ACTIVE';
CREATE INDEX idx_equipment_available_inventory ON inventory_equipments(player_id, availability)
    WHERE equipped = FALSE;
ALTER TABLE auction_transactions ALTER COLUMN item_definition_id DROP NOT NULL;
