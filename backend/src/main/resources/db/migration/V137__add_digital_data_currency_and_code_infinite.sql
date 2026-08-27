ALTER TABLE players
    ADD COLUMN digital_data INTEGER NOT NULL DEFAULT 0;

INSERT INTO item_definitions (
    code, name, description, category, stackable,
    buy_price, sell_price, tradable, sellable, usable,
    max_stack, rarity, icon
)
VALUES (
    'CODE_INFINITE',
    'Código Infinito',
    'Código raro da Infinity Mountain usado para aumentar o IV mínimo de HP, ATK ou DEF durante o Rebirth.',
    'REFINEMENT_MATERIAL',
    TRUE,
    NULL, NULL, FALSE, FALSE, FALSE,
    999, 'LEGENDARY', 'code_infinite'
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
