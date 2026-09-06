BEGIN;

INSERT INTO loot_tables (
    code, name, description, active, min_items, max_items, created_by, updated_by
)
VALUES
    (
        'LOOT_TABLE_CHEST_INCUBATOR',
        'Baú de Incubadoras',
        'Tabela de recompensas do Baú de Incubadoras.',
        TRUE, 1, 1, 'SYSTEM', 'SYSTEM'
    ),
    (
        'LOOT_TABLE_CHEST_INCUBATOR_PREMIUM',
        'Baú de Incubadoras Premium',
        'Tabela de recompensas do Baú de Incubadoras Premium.',
        TRUE, 1, 1, 'SYSTEM', 'SYSTEM'
    )
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    min_items = EXCLUDED.min_items,
    max_items = EXCLUDED.max_items,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO loot_table_rarity_weights (loot_table_id, rarity, weight)
SELECT lt.id, data.rarity, data.weight
FROM loot_tables lt
JOIN (VALUES
    ('LOOT_TABLE_CHEST_INCUBATOR', 'COMMON', 55),
    ('LOOT_TABLE_CHEST_INCUBATOR', 'RARE', 30),
    ('LOOT_TABLE_CHEST_INCUBATOR', 'EPIC', 12),
    ('LOOT_TABLE_CHEST_INCUBATOR', 'LEGENDARY', 3),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'COMMON', 0),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'RARE', 45),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'EPIC', 40),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'LEGENDARY', 15)
) AS data(table_code, rarity, weight) ON data.table_code = lt.code
ON CONFLICT (loot_table_id, rarity) DO UPDATE SET
    weight = EXCLUDED.weight;

INSERT INTO loot_table_entries (
    loot_table_id, rarity, item_type, material_code, weight,
    min_quantity, max_quantity, active
)
SELECT lt.id, data.rarity, data.item_type, NULL, 1,
       data.min_quantity, data.max_quantity, TRUE
FROM loot_tables lt
JOIN (VALUES
    ('LOOT_TABLE_CHEST_INCUBATOR', 'COMMON', 'INCUBATOR_COMMON', 1, 2),
    ('LOOT_TABLE_CHEST_INCUBATOR', 'RARE', 'INCUBATOR_RARE', 1, 1),
    ('LOOT_TABLE_CHEST_INCUBATOR', 'EPIC', 'INCUBATOR_EPIC', 1, 1),
    ('LOOT_TABLE_CHEST_INCUBATOR', 'LEGENDARY', 'INCUBATOR_LEGENDARY', 1, 1),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'RARE', 'INCUBATOR_RARE', 1, 2),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'EPIC', 'INCUBATOR_EPIC', 1, 1),
    ('LOOT_TABLE_CHEST_INCUBATOR_PREMIUM', 'LEGENDARY', 'INCUBATOR_LEGENDARY', 1, 1)
) AS data(table_code, rarity, item_type, min_quantity, max_quantity)
    ON data.table_code = lt.code
WHERE NOT EXISTS (
    SELECT 1
    FROM loot_table_entries existing
    WHERE existing.loot_table_id = lt.id
      AND existing.rarity = data.rarity
      AND existing.item_type = data.item_type
      AND existing.material_code IS NULL
);

INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
    (
        'CHEST_INCUBATOR',
        'Baú de Incubadoras',
        'Contém uma incubadora aleatória (Comum a Lendária).',
        'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'RARE', 'chest_incubator'
    ),
    (
        'CHEST_INCUBATOR_PREMIUM',
        'Baú de Incubadoras Premium',
        'Contém uma incubadora aleatória Rara, Épica ou Lendária.',
        'CHEST', TRUE, NULL, NULL, TRUE, TRUE, TRUE, 999, 'EPIC', 'chest_incubator_premium'
    )
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    buy_price = EXCLUDED.buy_price,
    sell_price = EXCLUDED.sell_price,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

INSERT INTO chest_definitions (
    code, name, description, icon, loot_table_id, item_definition_id,
    tradable, active, created_by, updated_by
)
SELECT data.code, data.name, data.description, data.icon,
       lt.id, item.id, TRUE, TRUE, 'SYSTEM', 'SYSTEM'
FROM (VALUES
    (
        'CHEST_INCUBATOR',
        'Baú de Incubadoras',
        'Contém uma incubadora aleatória (Comum a Lendária).',
        'chest_incubator',
        'LOOT_TABLE_CHEST_INCUBATOR'
    ),
    (
        'CHEST_INCUBATOR_PREMIUM',
        'Baú de Incubadoras Premium',
        'Contém uma incubadora aleatória Rara, Épica ou Lendária.',
        'chest_incubator_premium',
        'LOOT_TABLE_CHEST_INCUBATOR_PREMIUM'
    )
) AS data(code, name, description, icon, table_code)
JOIN loot_tables lt ON lt.code = data.table_code
JOIN item_definitions item ON item.code = data.code
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    loot_table_id = EXCLUDED.loot_table_id,
    item_definition_id = EXCLUDED.item_definition_id,
    tradable = EXCLUDED.tradable,
    active = EXCLUDED.active,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO shop_products (
    code, name, description, product_type, category, item_type,
    item_definition_code, price, sell_price, active, created_by, updated_by
)
VALUES (
    'CHEST_INCUBATOR',
    'Baú de Incubadoras',
    'Contém uma incubadora aleatória (Comum a Lendária).',
    'ITEM',
    'CHEST',
    'LOOT_CHEST',
    'CHEST_INCUBATOR',
    800,
    0,
    TRUE,
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    product_type = EXCLUDED.product_type,
    category = EXCLUDED.category,
    item_type = EXCLUDED.item_type,
    item_definition_code = EXCLUDED.item_definition_code,
    price = EXCLUDED.price,
    sell_price = EXCLUDED.sell_price,
    active = EXCLUDED.active,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

ALTER TABLE arena_shop_products
    ADD COLUMN IF NOT EXISTS item_definition_code VARCHAR(100);

INSERT INTO arena_shop_products (
    code, name, product_type, item_type, item_definition_code,
    quantity, price_coins, active
)
VALUES (
    'ARENA_CHEST_INCUBATOR_PREMIUM',
    'Baú de Incubadoras Premium',
    'ITEM',
    'LOOT_CHEST',
    'CHEST_INCUBATOR_PREMIUM',
    1,
    900,
    TRUE
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    product_type = EXCLUDED.product_type,
    item_type = EXCLUDED.item_type,
    item_definition_code = EXCLUDED.item_definition_code,
    quantity = EXCLUDED.quantity,
    price_coins = EXCLUDED.price_coins,
    active = EXCLUDED.active;

COMMIT;
