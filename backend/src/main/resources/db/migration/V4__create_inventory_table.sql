CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    player_id UUID NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL
);