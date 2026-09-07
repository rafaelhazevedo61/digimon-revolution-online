-- A V164 removeu a linha evolutiva Mekamon (não oficial) e, como efeito colateral,
-- excluiu os item_definitions de FRAGMENT_THUNDERBALLMON, FRAGMENT_MEGADRAMON e
-- FRAGMENT_MACHINEDRAMON (a checagem "NOT EXISTS evolution_step_materials" passou
-- porque os próprios materiais da linha já haviam sido apagados na mesma transação).
--
-- A V233, mais tarde, voltou a referenciar esses códigos como material de baús de
-- fragmento por estágio (Campeão/Ultimate/Mega) sem recriar os itens, então quem
-- abre um desses baús e sorteia o material recebe o erro
-- "Reward item is not defined: FRAGMENT_THUNDERBALLMON" e a abertura é cancelada.
--
-- Esta migration recria apenas os itens de inventário usados como material de baú.
-- A linha evolutiva do Mekamon continua removida (ela não é reativada aqui) porque
-- não é um Digimon oficial; os fragmentos agora servem apenas como recompensa de
-- baú/fragmento, sem vínculo com uma evolução específica.
BEGIN;

INSERT INTO item_definitions (code, name, description, category, stackable, buy_price, sell_price, tradable, sellable, usable, max_stack, rarity, icon) VALUES
    ('FRAGMENT_THUNDERBALLMON', 'Fragmento do Thunderballmon', 'Fragmento colecionável obtido em baús de fragmento.', 'EVOLUTION_MATERIAL', TRUE, NULL, 60, TRUE, TRUE, FALSE, 999, 'RARE', 'fragment_champion_specific'),
    ('FRAGMENT_MEGADRAMON', 'Fragmento do Megadramon', 'Fragmento colecionável obtido em baús de fragmento.', 'EVOLUTION_MATERIAL', TRUE, NULL, 120, TRUE, TRUE, FALSE, 999, 'EPIC', 'fragment_ultimate_specific'),
    ('FRAGMENT_MACHINEDRAMON', 'Fragmento do Machinedramon', 'Fragmento colecionável obtido em baús de fragmento.', 'EVOLUTION_MATERIAL', TRUE, NULL, 250, TRUE, TRUE, FALSE, 999, 'LEGENDARY', 'fragment_mega_specific')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    sell_price = EXCLUDED.sell_price,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

COMMIT;
