ALTER TABLE item_definitions ADD COLUMN buy_price INT;
ALTER TABLE item_definitions ADD COLUMN sell_price INT;
ALTER TABLE item_definitions ADD COLUMN tradable BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE item_definitions ADD COLUMN sellable BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE item_definitions ADD COLUMN usable BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE item_definitions ADD COLUMN max_stack INT;
ALTER TABLE item_definitions ADD COLUMN rarity VARCHAR(20) NOT NULL DEFAULT 'COMMON';
ALTER TABLE item_definitions ADD COLUMN icon VARCHAR(120);
