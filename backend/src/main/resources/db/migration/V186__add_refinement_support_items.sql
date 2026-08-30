INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES
(
    'REFINEMENT_SUCCESS_BOOST',
    'Pergaminho de Refinamento',
    'Aumenta em 10 pontos percentuais a chance de sucesso de um refinamento.',
    'MATERIAL', true, NULL, NULL, true, true, false, 999, 'RARE', 'refinement_success_boost'
),
(
    'REFINEMENT_PROTECTION',
    'Cristal de Proteção',
    'Impede que o equipamento seja destruído caso ocorra uma quebra no refinamento +10 para +11.',
    'MATERIAL', true, NULL, NULL, true, true, false, 999, 'EPIC', 'refinement_protection'
)
ON CONFLICT (code) DO NOTHING;
