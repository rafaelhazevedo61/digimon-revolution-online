INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES (
    'ASCENSION_CORE',
    'Núcleo de Ascensão',
    'Material usado para ascender equipamentos completamente refinados.',
    'REFINEMENT_MATERIAL',
    TRUE, NULL, NULL, TRUE, TRUE, FALSE, 999, 'EPIC', 'ascension_core'
)
ON CONFLICT (code) DO NOTHING;
