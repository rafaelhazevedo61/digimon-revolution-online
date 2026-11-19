-- Item consumível que amplia a capacidade de incubação do jogador em um slot.
INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES (
    'INCUBATION_SLOT_UNLOCK',
    'Expansor de Slot de Incubação',
    'Desbloqueia mais um slot de incubação para o jogador, até o limite de três.',
    'CONSUMABLE', TRUE, NULL, 500,
    TRUE, TRUE, TRUE, 3, 'EPIC', 'incubation_slot_unlock'
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
    'INCUBATION_SLOT_UNLOCK',
    'Expansor de Slot de Incubação',
    'Consumível que desbloqueia mais um slot de incubação para o jogador.',
    'ITEM', 'CONSUMABLE', 'INCUBATION_SLOT_UNLOCK',
    'INCUBATION_SLOT_UNLOCK', NULL, 2000, 500, TRUE
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
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';
