ALTER TABLE players
    ADD COLUMN unlocked_incubation_slots INTEGER NOT NULL DEFAULT 1;

ALTER TABLE players
    ADD CONSTRAINT chk_players_unlocked_incubation_slots
        CHECK (unlocked_incubation_slots BETWEEN 1 AND 3);

ALTER TABLE incubations
    ADD COLUMN slot_number INTEGER NOT NULL DEFAULT 1;

ALTER TABLE incubations
    ADD CONSTRAINT chk_incubations_slot_number
        CHECK (slot_number BETWEEN 1 AND 3);

CREATE UNIQUE INDEX ux_incubations_player_slot_active
    ON incubations (player_id, slot_number)
    WHERE status <> 'CLAIMED';
