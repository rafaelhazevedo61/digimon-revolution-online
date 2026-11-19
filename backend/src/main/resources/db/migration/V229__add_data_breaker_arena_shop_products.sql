ALTER TABLE arena_shop_products
    ADD COLUMN product_type VARCHAR(20) NOT NULL DEFAULT 'ITEM',
    ADD COLUMN equipment_template_name VARCHAR(120);

INSERT INTO arena_shop_products
    (code, name, item_type, quantity, price_coins, active, product_type, equipment_template_name)
VALUES
    ('ARENA_DATA_BREAKER_GARRA', 'Garra Data Breaker (raridade aleatória)', 'EQUIPMENT', 1, 2000, TRUE, 'EQUIPMENT', 'Garra Data Breaker T1'),
    ('ARENA_DATA_BREAKER_FRAGMENTO', 'Fragmento Data Breaker (raridade aleatória)', 'EQUIPMENT', 1, 2000, TRUE, 'EQUIPMENT', 'Fragmento Data Breaker T1'),
    ('ARENA_DATA_BREAKER_COURACA', 'Couraça Data Breaker (raridade aleatória)', 'EQUIPMENT', 1, 2500, TRUE, 'EQUIPMENT', 'Couraça Data Breaker T1');
