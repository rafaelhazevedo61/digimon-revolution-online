ALTER TABLE players
    ADD COLUMN max_team_slots INTEGER NOT NULL DEFAULT 3;

ALTER TABLE players
    ADD CONSTRAINT chk_players_max_team_slots
        CHECK (max_team_slots BETWEEN 3 AND 10);

INSERT INTO item_definitions (
    code, name, description, category, stackable,
    buy_price, sell_price, tradable, sellable, usable,
    max_stack, rarity, icon
)
VALUES (
    'TEAM_SLOT_UNLOCK',
    'Expansor de Slot de Time',
    'Aumenta em 1 a capacidade máxima de times, até o limite de 10 times.',
    'CONSUMABLE', TRUE,
    NULL, NULL, FALSE, FALSE, TRUE,
    10, 'EPIC', 'team_slot_unlock'
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

INSERT INTO shop_products (
    code, name, description, product_type, category, item_type,
    item_definition_code, equipment_template_name, price, sell_price, active
)
VALUES (
    'TEAM_SLOT_UNLOCK',
    'Expansor de Slot de Time',
    'Aumenta em 1 a capacidade máxima de times, até o limite de 10 times.',
    'ITEM', 'CONSUMABLE', 'TEAM_SLOT_UNLOCK',
    'TEAM_SLOT_UNLOCK', NULL, 1000000, 0, TRUE
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    product_type = EXCLUDED.product_type,
    category = EXCLUDED.category,
    item_type = EXCLUDED.item_type,
    item_definition_code = EXCLUDED.item_definition_code,
    equipment_template_name = EXCLUDED.equipment_template_name,
    price = EXCLUDED.price,
    sell_price = EXCLUDED.sell_price,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';
