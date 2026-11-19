ALTER TABLE inventory_equipments
    ADD COLUMN locked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_inventory_equipments_player_locked
    ON inventory_equipments (player_id, locked)
    WHERE equipped = FALSE;
