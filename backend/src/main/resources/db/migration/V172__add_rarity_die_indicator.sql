ALTER TABLE digimons
    ADD COLUMN rarity_changed_by_die BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN original_rarity_before_die VARCHAR(20),
    ADD COLUMN rarity_changed_by_die_at TIMESTAMP;
