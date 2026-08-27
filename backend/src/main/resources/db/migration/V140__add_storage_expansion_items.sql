-- Itens consumíveis que ampliam permanentemente a capacidade do Storage.
-- A distribuição nos drops permanece sob configuração manual do Admin.
INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
    (
        'STORAGE_SLOT_1',
        '+1 Storage',
        'Aumenta permanentemente a capacidade do Storage em 1 espaço.',
        'CONSUMABLE', TRUE, NULL, NULL,
        TRUE, FALSE, TRUE, 999, 'COMMON', 'storage_slot_1'
    ),
    (
        'STORAGE_SLOT_5',
        '+5 Storage',
        'Aumenta permanentemente a capacidade do Storage em 5 espaços.',
        'CONSUMABLE', TRUE, NULL, NULL,
        TRUE, FALSE, TRUE, 999, 'RARE', 'storage_slot_5'
    ),
    (
        'STORAGE_SLOT_10',
        '+10 Storage',
        'Aumenta permanentemente a capacidade do Storage em 10 espaços.',
        'CONSUMABLE', TRUE, NULL, NULL,
        TRUE, FALSE, TRUE, 999, 'EPIC', 'storage_slot_10'
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

-- Os itens não são associados automaticamente a drops nesta migration.
-- A configuração dos drops será feita manualmente pelo Admin.
