UPDATE item_definitions
SET name = 'Dado de Raridade',
    description = 'Usa este dado para gerar uma nova proposta de raridade para o seu Digimon ativo.',
    icon = '🎲'
WHERE code = 'RARITY_REROLL';
