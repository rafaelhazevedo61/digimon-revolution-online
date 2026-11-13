ALTER TABLE players
    ADD COLUMN energy_cell_purchase_date DATE,
    ADD COLUMN energy_cells_purchased INTEGER NOT NULL DEFAULT 0;

INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity
)
VALUES (
    'ENERGY_CELL', 'Célula de Energia', 'Recupera 10% da energia máxima do Digimon ativo.',
    'CONSUMABLE', TRUE, 100, 0, FALSE, FALSE, TRUE, 999, 'COMMON'
);

INSERT INTO shop_products (
    code, name, description, product_type, category, item_type,
    item_definition_code, price, sell_price, active
)
VALUES (
    'ENERGY_CELL', 'Célula de Energia',
    'Recupera 10% da energia máxima do Digimon ativo. Limite de 3 compras por dia, com reinício à meia-noite UTC.',
    'ITEM', 'POTION', 'ENERGY_CELL', 'ENERGY_CELL', 100, 0, TRUE
);
