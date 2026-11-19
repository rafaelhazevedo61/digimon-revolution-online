ALTER TABLE players ADD COLUMN vip_energy_charge_claim_date DATE;

INSERT INTO item_definitions (
    code, name, description, category, stackable, tradable, sellable, usable, max_stack, rarity
)
VALUES (
    'VIP_ENERGY_CHARGE', 'Carga de Energia Total',
    'Preenche a barra de energia do Digimon ativo.',
    'CONSUMABLE', TRUE, FALSE, FALSE, TRUE, 999, 'COMMON'
);
