-- Núcleos usados na progressão de equipamentos T1-T10.
INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
('BASIC_ENHANCEMENT_CORE', 'Núcleo de Aprimoramento', 'Necessário para aprimorar equipamentos até T4.', 'MATERIAL', TRUE, NULL, NULL, TRUE, TRUE, FALSE, 999, 'COMMON', 'basic_enhancement_core'),
('ADVANCED_ENHANCEMENT_CORE', 'Núcleo Avançado de Aprimoramento', 'Necessário para aprimorar equipamentos de T5 a T7.', 'MATERIAL', TRUE, NULL, NULL, TRUE, TRUE, FALSE, 999, 'RARE', 'advanced_enhancement_core'),
('SUPREME_ENHANCEMENT_CORE', 'Núcleo Supremo de Aprimoramento', 'Necessário para aprimorar equipamentos de T8 a T10.', 'MATERIAL', TRUE, NULL, NULL, TRUE, TRUE, FALSE, 999, 'LEGENDARY', 'supreme_enhancement_core')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;
