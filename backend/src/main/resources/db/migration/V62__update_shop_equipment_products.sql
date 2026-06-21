-- Remover produtos da loja que referenciam equipment templates antigos
DELETE FROM shop_products WHERE product_type = 'EQUIPMENT';

-- Inserir novos produtos de equipamento T1 (disponíveis na loja para compra)
INSERT INTO shop_products (code, name, description, product_type, category, item_type, equipment_template_name, price, sell_price, active) VALUES
-- Set Berserker T1
('BERSERKER_WEAPON_T1', 'Garra Berserker T1', 'Arma do set Berserker. Foco em ATK.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Garra Berserker T1', 300, 75, true),
('BERSERKER_ARMOR_T1', 'Couraça Berserker T1', 'Armadura do set Berserker.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Couraça Berserker T1', 300, 75, true),
('BERSERKER_ACCESSORY_T1', 'Emblema Berserker T1', 'Acessório do set Berserker.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Emblema Berserker T1', 300, 75, true),
-- Set Guardião T1
('GUARDIAN_WEAPON_T1', 'Lança Guardiã T1', 'Arma do set Guardião. Foco em DEF/HP.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Lança Guardiã T1', 300, 75, true),
('GUARDIAN_ARMOR_T1', 'Armadura Guardiã T1', 'Armadura do set Guardião.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Armadura Guardiã T1', 300, 75, true),
('GUARDIAN_ACCESSORY_T1', 'Medalha Guardiã T1', 'Acessório do set Guardião.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Medalha Guardiã T1', 300, 75, true),
-- Set Vitalidade T1
('VITALITY_WEAPON_T1', 'Cetro da Vitalidade T1', 'Arma do set Vitalidade. Foco em HP.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Cetro da Vitalidade T1', 300, 75, true),
('VITALITY_ARMOR_T1', 'Vestes da Vitalidade T1', 'Armadura do set Vitalidade.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Vestes da Vitalidade T1', 300, 75, true),
('VITALITY_ACCESSORY_T1', 'Emblema da Vitalidade T1', 'Acessório do set Vitalidade.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Emblema da Vitalidade T1', 300, 75, true),
-- Set Equilibrado T1
('BALANCED_WEAPON_T1', 'Lâmina do Equilíbrio T1', 'Arma do set Equilibrado. Escala tudo.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Lâmina do Equilíbrio T1', 300, 75, true),
('BALANCED_ARMOR_T1', 'Armadura do Equilíbrio T1', 'Armadura do set Equilibrado.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Armadura do Equilíbrio T1', 300, 75, true),
('BALANCED_ACCESSORY_T1', 'Símbolo do Equilíbrio T1', 'Acessório do set Equilibrado.', 'EQUIPMENT', 'EQUIPMENT', NULL, 'Símbolo do Equilíbrio T1', 300, 75, true);

-- Adicionar Pedra de Refinamento na loja
INSERT INTO shop_products (code, name, description, product_type, category, item_type, equipment_template_name, price, sell_price, active) VALUES
('REFINEMENT_STONE_SHOP', 'Pedra de Refinamento', 'Usada para refinar equipamentos.', 'ITEM', 'CONSUMABLE', 'REFINEMENT_STONE', NULL, 500, 100, true);
