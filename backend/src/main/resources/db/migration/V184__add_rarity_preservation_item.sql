INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES (
    'RARITY_PRESERVATION',
    'Cristal de Preservação de Raridade',
    'Garante que o Digimon renascido mantenha a mesma raridade do Digimon original.',
    'CONSUMABLE',
    TRUE, NULL, NULL, FALSE, FALSE, FALSE, 999, 'LEGENDARY', 'rarity_preservation'
)
ON CONFLICT (code) DO NOTHING;
