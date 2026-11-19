-- Adicionar refinement_level e template_id à tabela de equipamentos do inventário
ALTER TABLE inventory_equipments ADD COLUMN refinement_level INT NOT NULL DEFAULT 0;
ALTER TABLE inventory_equipments ADD COLUMN set_code VARCHAR(30);
ALTER TABLE inventory_equipments ADD COLUMN tier INT;

-- Adicionar item de refinamento no catálogo de itens
INSERT INTO item_definitions (code, name, description, category, stackable, buy_price, sell_price, tradable, sellable, usable, max_stack, rarity, icon)
VALUES ('REFINEMENT_STONE', 'Pedra de Refinamento', 'Usada para refinar equipamentos, aumentando seus atributos.', 'MATERIAL', true, 500, 100, true, true, false, 999, 'RARE', 'refinement_stone')
ON CONFLICT (code) DO NOTHING;
