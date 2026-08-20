ALTER TABLE loot_table_rarity_weights
    DROP CONSTRAINT ck_loot_table_rarity_weight_positive;

ALTER TABLE loot_table_rarity_weights
    ADD CONSTRAINT ck_loot_table_rarity_weight_non_negative CHECK (weight >= 0);

ALTER TABLE loot_tables
    ADD COLUMN min_items INT NOT NULL DEFAULT 1,
    ADD COLUMN max_items INT NOT NULL DEFAULT 4;

ALTER TABLE loot_tables
    ADD CONSTRAINT ck_loot_table_item_count_range
    CHECK (min_items >= 1 AND max_items >= min_items AND max_items <= 4);
